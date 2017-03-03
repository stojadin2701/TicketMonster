/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bean;

import dao.EventDAO;
import dao.ImageDAO;
import dao.ReservationDAO;
import dao.TicketsRemainingDAO;
import dao.UserReviewDAO;
import dao.VideoDAO;
import entity.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.faces.view.ViewScoped;
import javax.transaction.Transactional;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.primefaces.event.FileUploadEvent;
import util.HibernateUtil;

/**
 *
 * @author Nemanja
 */
@Named(value = "eventDetailsBean")
@ViewScoped
public class EventDetailsBean implements Serializable {

    @ManagedProperty(value = "#{loginBean}")
    private LoginBean loginBean;

    private Event event;

    private List<Image> ilist;
    private List<Video> vlist;

    private Integer daySelected;

    private List<Integer> availableDays;

    private List<UserReview> userReviews;

    private boolean hasTickets;

    private boolean reservable;

    private boolean finished;

    private boolean boughtTicket;

    private String currentComment;

    private Byte currentRating;

    public synchronized void reserveAll(User usr) {
        if (canReserveMore(usr)) {
            if ((new Date()).after(event.getEndDate())) {
                FacesContext.getCurrentInstance().addMessage("messageReservation", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "You cannot reserve a ticket for an event that has ended."));
            } else if (!TicketsRemainingDAO.canBuyAll(event)) {
                FacesContext.getCurrentInstance().addMessage("messageReservation", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "Some of the days do not have tickets available."));
            } else {
                TicketsRemainingDAO.reduceAll(event);
                ReservationDAO.reserve(usr, event, true, null);
                FacesContext.getCurrentInstance().addMessage("messageReservation", new FacesMessage(FacesMessage.SEVERITY_INFO, "Info:", "You have succesfuly reserved an all-days ticket for " + event.getEventName() + "."));
            }
        } else {
            FacesContext.getCurrentInstance().addMessage("messageReservation", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "You cannot make more than " + event.getMaxReservationsPerUser() + " reservations for this event."));
        }
    }

    public synchronized boolean canReserveMore(User usr) {
        short nue = ReservationDAO.numOfReservationsUserEvent(usr, event);
        short max = event.getMaxReservationsPerUser();
        if (nue < max) {
            return true;
        } else {
            return false;
        }
    }

    public void reserveOne(User usr) {
        if (canReserveMore(usr)) {
            if (daySelected == null) {
                FacesContext.getCurrentInstance().addMessage("messageReservation", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "You must choose a day."));
            } else {
                boolean ok = false;
                for (Integer dayNum : availableDays) {
                    if (dayNum.intValue() == daySelected.intValue()) {
                        ok = true;
                        break;
                    }
                }
                if (!ok) {
                    FacesContext.getCurrentInstance().addMessage("messageReservation", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "You must choose a day which has remaining tickets."));
                } else {
                    TicketsRemainingDAO.reduceOne(event, daySelected);
                    ReservationDAO.reserve(usr, event, false, daySelected);
                    FacesContext.getCurrentInstance().addMessage("messageReservation", new FacesMessage(FacesMessage.SEVERITY_INFO, "Info:", "You have succesfuly reserved a one-day ticket for " + event.getEventName() + "."));
                }
            }
        } else {
            FacesContext.getCurrentInstance().addMessage("messageReservation", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "You cannot make more than " + event.getMaxReservationsPerUser() + " reservations for this event."));
        }

    }

    public void stripImageUrl() {
        for (int i = 0; i < ilist.size(); i++) {
            String[] values = ilist.get(i).getImageUrl().split("\\\\");
            ilist.get(i).setImageUrl(values[values.length - 1]);
        }
    }

    public void stripVideoUrl() {
        for (int i = 0; i < vlist.size(); i++) {
            String[] values = vlist.get(i).getVideoUrl().split("\\\\");
            vlist.get(i).setVideoUrl(values[values.length - 1]);
        }
    }

    public EventDetailsBean() {
        FacesContext context = FacesContext.getCurrentInstance();
        loginBean = context.getApplication().evaluateExpressionGet(context, "#{loginBean}", LoginBean.class);
        event = loginBean.getCurrentEvent();
        EventDAO.incrementViewCount(event);
        ilist = EventDAO.getApprovedImages(event);
        vlist = EventDAO.getApprovedVideos(event);
        //stripImageUrl();
        //stripVideoUrl();

        reservable = !event.isCancelled() && (new Date()).before(event.getEndDate());
        availableDays = TicketsRemainingDAO.getAvailableDays(event);
        hasTickets = availableDays != null && !availableDays.isEmpty();
        finished = event.getEndDate().before(new Date());
        boughtTicket = ReservationDAO.boughtTicket(event, loginBean.getCurrentUser());
        userReviews = UserReviewDAO.getForEventName(event.getEventName());
    }

    public boolean canComment(User usr) {
        return ReservationDAO.boughtTicket(event, usr) && finished;
    }

    public void review(User usr) {
        if (usr != null) {
            if ((currentComment == null || "".equals(currentComment)) && (currentRating == null)) {
                FacesContext.getCurrentInstance().addMessage("messageReview", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "You must rate the event or add a comment."));
            } else {
                UserReview ur = new UserReview(usr, event.getEventName(), currentComment, currentRating, new Date());
                UserReviewDAO.store(ur);
                if (currentRating != null) {
                    EventDAO.updateRating(event.getEventName());
                }
                userReviews.add(ur);
            }
        }
        currentComment = null;
        currentRating = null;
    }

    public String addFile(FileUploadEvent ev, String type) {
        Path filePath = null;
        Path dirStruct = Paths.get("/PIA_WEB/event_files/" + event.getEventName() + "/" + type + "/");
        try (InputStream input = ev.getFile().getInputstream()) {
            Files.createDirectories(dirStruct);
            String[] fileInfo = ev.getFile().getFileName().split("\\.(?=[^\\.]+$)");
            filePath = Files.createTempFile(dirStruct, fileInfo[0], "." + fileInfo[1]);
            Files.copy(input, filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            filePath = null;
            Logger.getLogger(AddFestivalBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return filePath != null ? filePath.toAbsolutePath().toString() : null;
    }

    public void addAndStoreImage(FileUploadEvent ev) {
        String filePath = addFile(ev, "img");
        if (filePath != null) {
            Image img = new Image();
            img.setEvent(event);
            img.setApproved(false);
            img.setImageUrl(filePath);
            ImageDAO.store(img);
            FacesContext.getCurrentInstance().addMessage("imageVideoMessage", new FacesMessage(FacesMessage.SEVERITY_INFO, "Info:", "You have succesfuly submitted " + ev.getFile().getFileName() + ". Come back later and check if the admin has approved your image."));
        }
    }

    public void addAndStoreVideo(FileUploadEvent ev) {
        String filePath = addFile(ev, "vid");
        if (filePath != null) {
            Video vid = new Video();
            vid.setEvent(event);
            vid.setApproved(false);
            vid.setVideoUrl(filePath);
            VideoDAO.store(vid);
            FacesContext.getCurrentInstance().addMessage("imageVideoMessage", new FacesMessage(FacesMessage.SEVERITY_INFO, "Info:", "You have succesfuly submitted " + ev.getFile().getFileName() + ". Come back later and check if the admin has approved your video."));
        }
    }

    /*@Transactional
    @PostConstruct
    private void initEvent() {
        event = (Event) FacesContext.getCurrentInstance().getExternalContext().getFlash().get("event");
        //event = (Event) FacesContext.getCurrentInstance().getExternalContext().getRequestMap().get("event");
        if (event == null) {
            event = new Event();
        }
    //    else {
     //       Session session = HibernateUtil.getSessionFactory().openSession();
      //      Transaction transaction = session.beginTransaction();
       //     Hibernate.initialize(event.getPerformers());
        //    transaction.commit();
         //}
    }*/
    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public boolean isReservable() {
        return reservable;
    }

    public void setReservable(boolean reservable) {
        this.reservable = reservable;
    }

    public LoginBean getLoginBean() {
        return loginBean;
    }

    public void setLoginBean(LoginBean loginBean) {
        this.loginBean = loginBean;
    }

    public List<Image> getIlist() {
        return ilist;
    }

    public void setIlist(List<Image> ilist) {
        this.ilist = ilist;
    }

    public List<Video> getVlist() {
        return vlist;
    }

    public void setVlist(List<Video> vlist) {
        this.vlist = vlist;
    }

    public Integer getDaySelected() {
        return daySelected;
    }

    public void setDaySelected(Integer daySelected) {
        this.daySelected = daySelected;
    }

    public boolean isHasTickets() {
        return hasTickets;
    }

    public void setHasTickets(boolean hasTickets) {
        this.hasTickets = hasTickets;
    }

    public List<Integer> getAvailableDays() {
        return availableDays;
    }

    public void setAvailableDays(List<Integer> availableDays) {
        this.availableDays = availableDays;
    }

    public List<UserReview> getUserReviews() {
        return userReviews;
    }

    public void setUserReviews(List<UserReview> userReviews) {
        this.userReviews = userReviews;
    }

    public boolean getFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public boolean getBoughtTicket() {
        return boughtTicket;
    }

    public void setBoughtTicket(boolean boughtTicket) {
        this.boughtTicket = boughtTicket;
    }

    public String getCurrentComment() {
        return currentComment;
    }

    public void setCurrentComment(String currentComment) {
        this.currentComment = currentComment;
    }

    public Byte getCurrentRating() {
        return currentRating;
    }

    public void setCurrentRating(Byte currentRating) {
        this.currentRating = currentRating;
    }

}
