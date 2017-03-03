/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bean;

import com.google.gson.Gson;
import entity.*;
import java.io.Serializable;
import javax.inject.Named;
import javax.faces.view.ViewScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import org.primefaces.event.FlowEvent;
import dao.*;
import file_loading.EventJSON;
import file_loading.EventJSONWrap;
import file_loading.PerformerJSON;
import file_loading.SocialLinkJSON;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ManagedProperty;
import org.primefaces.event.FileUploadEvent;
import java.util.Date;

/**
 *
 * @author Nemanja
 */
@Named(value = "editEventBean")
@ViewScoped
public class EditEventBean implements Serializable {

    @ManagedProperty(value = "#{loginBean}")
    private LoginBean loginBean;

    private Event ev = new Event();
    private Performer per = new Performer();

    private List<Performer> plist;
    private List<SocialLink> llist;
    private List<Image> ilist;
    private List<Video> vlist;

    private List<Performer> removedPerformerList = new ArrayList<>();
    private List<SocialLink> removedSocialLinkList = new ArrayList<>();
    private List<Image> removedImageList = new ArrayList<>();
    private List<Video> removedVideoList = new ArrayList<>();

    private String fbLink = null;
    private String twLink = null;
    private String instaLink = null;
    private String ytLink = null;

    private final Date currentDate = new Date();

    private int prevMaxTicketsAvailablePerDay;

    private boolean perfChanged;

    private java.util.Date prevStartDate;
    private java.util.Date prevEndDate;

    public void reinitialize() {
        ev = new Event();
        per = new Performer();
        plist.clear();
        ilist.clear();
        llist.clear();
        vlist.clear();
        fbLink = null;
        twLink = null;
        instaLink = null;
        ytLink = null;
    }

    public Date getCurrentDate() {
        return currentDate;
    }

    public EditEventBean() {
        FacesContext context = FacesContext.getCurrentInstance();
        loginBean = context.getApplication().evaluateExpressionGet(context, "#{loginBean}", LoginBean.class);
        ev = loginBean.getCurrentEvent();
        prevMaxTicketsAvailablePerDay = ev.getMaxTicketsAvailablePerDay();
        prevStartDate = ev.getStartDate();
        prevEndDate = ev.getEndDate();
        plist = new ArrayList<>(ev.getPerformers());
        ilist = new ArrayList<>(ev.getImages());
        vlist = new ArrayList<>(ev.getVideos());
        llist = new ArrayList<>(ev.getSocialLinks());
        for (SocialLink link : llist) {
            if ("Facebook".equals(link.getNetworkName())) {
                fbLink = link.getUrl();
            }
            if ("Twitter".equals(link.getNetworkName())) {
                twLink = link.getUrl();
            }
            if ("Instagram".equals(link.getNetworkName())) {
                instaLink = link.getUrl();
            }
            if ("YouTube".equals(link.getNetworkName())) {
                ytLink = link.getUrl();
            }
        }
    }

    public void removePerformer(Performer p) {
        if (p != null) {
            plist.remove(p);
            removedPerformerList.add(p);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Info:", p.getPerformerName() + " removed from the event."));
            perfChanged = true;
        }
    }

    public void update() {
        if (prevMaxTicketsAvailablePerDay <= ev.getMaxTicketsAvailablePerDay()) {
            EventDAO.update(ev);

            if (ev.getStartDate().compareTo(prevStartDate) != 0 || ev.getEndDate().compareTo(prevEndDate) != 0) {
                ev.getReservations().forEach(res -> MessageDAO.addFromReservation((Reservation) res, MessageDAO.TIMETABLE_CHANGED));
            }

            if (perfChanged) {
                ev.getReservations().forEach(res -> MessageDAO.addFromReservation((Reservation) res, MessageDAO.PERFORMER_CHANGED));
            }

            ImageDAO.removeMultiple(removedImageList);
            VideoDAO.removeMultiple(removedVideoList);
            PerformerDAO.removeMultiple(removedPerformerList);

            ImageDAO.saveOrUpdateMultiple(ilist);
            VideoDAO.saveOrUpdateMultiple(vlist);
            PerformerDAO.saveOrUpdateMultiple(plist);

            for (SocialLink link : llist) {
                if ("Facebook".equals(link.getNetworkName()) && !fbLink.equals(link.getUrl())) {
                    link.setUrl(fbLink);
                    if (fbLink != null && !fbLink.isEmpty()) {
                        SocialLinkDAO.update(link);
                    } else {
                        SocialLinkDAO.delete(link);
                    }
                }
                if ("Twitter".equals(link.getNetworkName()) && !twLink.equals(link.getUrl())) {
                    link.setUrl(twLink);
                    if (twLink != null && !twLink.isEmpty()) {
                        SocialLinkDAO.update(link);
                    } else {
                        SocialLinkDAO.delete(link);
                    }
                }
                if ("Instagram".equals(link.getNetworkName()) && !instaLink.equals(link.getUrl())) {
                    link.setUrl(instaLink);
                    if (instaLink != null && !instaLink.isEmpty()) {
                        SocialLinkDAO.update(link);
                    } else {
                        SocialLinkDAO.delete(link);
                    }
                }
                if ("YouTube".equals(link.getNetworkName()) && !ytLink.equals(link.getUrl())) {
                    link.setUrl(ytLink);
                    if (ytLink != null && !ytLink.isEmpty()) {
                        SocialLinkDAO.update(link);
                    } else {
                        SocialLinkDAO.delete(link);
                    }
                }
            }

            //    SocialLinkDAO.storeMultiple(llist);
            //    ImageDAO.storeMultiple(ilist);
            //    VideoDAO.storeMultiple(vlist);
            //    PerformerDAO.storeMultiple(plist);
            int dayDiff = 0;
//
//            LocalDate ldStart = ev.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
//            LocalDate ldEnd = ev.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
//
//            while (!ldStart.isAfter(ldEnd)) {
//                ldStart = ldStart.plusDays(1);
//                dayDiff++;
//            }
//            
//            ldStart = prevStartDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
//            ldEnd = prevEndDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
//            
//            while (!ldStart.isAfter(ldEnd)) {
//                ldStart = ldStart.plusDays(1);
//                dayDiff--;
//            }

            TicketsRemainingDAO.updateTicketsRemainingFromEvent(ev, ev.getMaxTicketsAvailablePerDay() - prevMaxTicketsAvailablePerDay, dayDiff);

            //TicketsRemainingDAO.addTicketsRemainingFromEvent(ev);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Info:", "Event modified."));
            try {
                FacesContext.getCurrentInstance().getExternalContext().redirect("index.xhtml");
                FacesContext.getCurrentInstance().responseComplete();
            } catch (IOException ex) {
                Logger.getLogger(AddFestivalBean.class.getName()).log(Level.SEVERE, null, ex);
            }

        } else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "You cannot reduce the maximum number of tickets."));
        }

    }

    public String onFlowProcess(FlowEvent event) {
        return event.getNewStep();
    }

    public String addFile(FileUploadEvent event, String type) {
        Path filePath = null;
        Path dirStruct;
        if ("img".equals(type)) {
            dirStruct = Paths.get("/PIA_WEB/event_files/" + ev.getEventName() + "/" + type + "/");
        } else {
            dirStruct = Paths.get("/PIA_WEB/videos/");
        }
        try (InputStream input = event.getFile().getInputstream()) {
            Files.createDirectories(dirStruct);
            String[] fileInfo = event.getFile().getFileName().split("\\.(?=[^\\.]+$)");
            filePath = Files.createTempFile(dirStruct, fileInfo[0], "." + fileInfo[1]);
            Files.copy(input, filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            filePath = null;
            Logger.getLogger(AddFestivalBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        if ("img".equals(type)) {
            return filePath != null ? filePath.toAbsolutePath().toString() : null;
        } else {
            return filePath != null ? "/videos/" + filePath.toString().substring(filePath.toString().lastIndexOf('\\') + 1) : null;
        }
    }

    public void addImage(FileUploadEvent event) {
        String filePath = addFile(event, "img");
        if (filePath != null) {
            Image img = new Image();
            img.setEvent(ev);
            img.setApproved(true);
            img.setImageUrl(filePath);
            ilist.add(img);
        }
    }

    public void removeImage(Image img) {
        ilist.remove(img);
        removedImageList.add(img);
    }

    public void removeVideo(Video vid) {
        vlist.remove(vid);
        removedVideoList.add(vid);
    }

    public void addVideo(FileUploadEvent event) {
        String filePath = addFile(event, "vid");
        if (filePath != null) {
            Video vid = new Video();
            vid.setEvent(ev);
            vid.setApproved(true);
            vid.setVideoUrl(filePath);
            vlist.add(vid);
        }
    }

    public void addPerformer() {
        boolean err = false;
        if ("".equals(per.getPerformerName())) {
            err = true;
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Error:", "Performer name cannot be empty"));
        }
        if (per.getStartTime() == null) {
            err = true;
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Error:", "Start time cannot be empty"));
        }
        if (per.getEndTime() == null) {
            err = true;
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Error:", "End time cannot be empty"));
        }
        if (per.getStartTime() != null && per.getEndTime() != null && per.getEndTime().before(per.getStartTime())) {
            err = true;
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Error:", "End time cannot be before start time"));
        }

        if (!err) {
            per.setEvent(ev);
            plist.add(per);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Info:", per.getPerformerName() + " added succesfully!"));
            per = new Performer();
            perfChanged = true;
        }
    }

    public void addEventFromFile(FileUploadEvent event) {
        String[] fileInfo = event.getFile().getFileName().split("\\.(?=[^\\.]+$)");
        if ("json".equals(fileInfo[1]) || "csv".equals(fileInfo[1])) {
            Path filePath = null;
            Path dirStruct = Paths.get("/PIA_WEB/temp/");
            try (InputStream input = event.getFile().getInputstream()) {
                Files.createDirectories(dirStruct);
                filePath = Files.createTempFile(dirStruct, fileInfo[0], "." + fileInfo[1]);
                Files.copy(input, filePath, StandardCopyOption.REPLACE_EXISTING);
                if ("json".equals(fileInfo[1])) {
                    readJSON(filePath.toAbsolutePath().toString(), event.getFile().getFileName());
                } else {
                    readCSV(filePath.toAbsolutePath().toString(), event.getFile().getFileName());
                }
            } catch (IOException ex) {
                Logger.getLogger(AddFestivalBean.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Wrong file format!"));
        }

    }

    public void readJSON(String filePath, String fileName) {
        try (BufferedReader breader = new BufferedReader(new FileReader(filePath))) {
            Gson gson = new Gson();
            EventJSONWrap evJSONwrap = gson.fromJson(breader, EventJSONWrap.class);
            EventJSON evj = evJSONwrap.getEvJSON();

            reinitialize();

            ev.setEventName(evj.getEventName());
            ev.setEventPlace(evj.getEventPlace());
            ev.setStartDate((new SimpleDateFormat("dd/MM/yyyy")).parse(evj.getStartDate()));
            ev.setEndDate((new SimpleDateFormat("dd/MM/yyyy")).parse(evj.getEndDate()));
            ev.setOneDayTicketPrice(Long.parseLong(evj.getTicketPrices().get(0)));
            ev.setAllDaysTicketPrice(Long.parseLong(evj.getTicketPrices().get(1)));
            ev.setMaxReservationsPerUser(Short.parseShort(evj.getMaxReservationsPerUser()));
            ev.setMaxTicketsAvailablePerDay(Integer.parseInt(evj.getMaxTicketsAvailablePerDay()));
            for (PerformerJSON pj : evj.getPerformers()) {
                per.setEvent(ev);
                per.setPerformerName(pj.getPerformerName());
                per.setStartTime((new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a")).parse(pj.getStartDate() + " " + pj.getStartTime()));
                per.setEndTime((new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a")).parse(pj.getEndDate() + " " + pj.getEndTime()));
                plist.add(per);
                per = new Performer();
            }
            for (SocialLinkJSON slj : evj.getSocialLinks()) {
                llist.add(new SocialLink(ev, slj.getNetworkName(), slj.getUrl()));
            }
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Info:", "File " + fileName + " successfully added."));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Error while reading " + fileName + "!"));
        }
    }

    public void readCSV(String filePath, String fileName) {
        try (BufferedReader breader = new BufferedReader(new FileReader(filePath))) {
            String line;
            String[] values;
            String regex = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";

            reinitialize();

            line = breader.readLine();
            if (!"\"Festival\",\"Place\",\"StartDate\",\"EndDate\"".equals(line)) {
                throw new Exception();
            }
            line = breader.readLine();
            values = line.split(regex, -1);
            trimQuotes(values);
            ev.setEventName(values[0]);
            ev.setEventPlace(values[1]);
            ev.setStartDate((new SimpleDateFormat("dd/MM/yyyy")).parse(values[2]));
            ev.setEndDate((new SimpleDateFormat("dd/MM/yyyy")).parse(values[3]));

            breader.readLine(); //\n

            line = breader.readLine();
            if (!"\"TicketType\",\"Price\"".equals(line)) {
                throw new Exception();
            }
            line = breader.readLine();
            values = line.split(regex, -1);
            trimQuotes(values);
            if (!"per day".equals(values[0])) {
                throw new Exception();
            }
            ev.setOneDayTicketPrice(Long.parseLong(values[1]));

            line = breader.readLine();
            values = line.split(regex, -1);
            trimQuotes(values);
            if (!"whole festival".equals(values[0])) {
                throw new Exception();
            }
            ev.setAllDaysTicketPrice(Long.parseLong(values[1]));

            breader.readLine(); //\n

            line = breader.readLine();
            if (!"\"Performer\",\"StartDate\",\"EndDate\",\"StartTime\",\"EndTime\"".equals(line)) {
                throw new Exception();
            }

            while (!"".equals(line = breader.readLine())) {
                values = line.split(regex, -1);
                trimQuotes(values);
                per.setEvent(ev);
                per.setPerformerName(values[0]);
                per.setStartTime((new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a")).parse(values[1] + " " + values[3]));
                per.setEndTime((new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a")).parse(values[2] + " " + values[4]));
                plist.add(per);
                per = new Performer();
            }

            line = breader.readLine();
            if (!"\"Social Network\",\"Link\"".equals(line)) {
                throw new Exception();
            }

            while (!"".equals(line = breader.readLine())) {
                values = line.split(regex, -1);
                trimQuotes(values);
                llist.add(new SocialLink(ev, values[0], values[1]));
            }

            line = breader.readLine();
            if (!"\"MaxReservationsPerUser\",\"MaxTicketsPerDay\"".equals(line)) {
                throw new Exception();
            }

            line = breader.readLine();
            values = line.split(regex, -1);
            trimQuotes(values);
            ev.setMaxReservationsPerUser(Short.parseShort(values[0]));
            ev.setMaxTicketsAvailablePerDay(Integer.parseInt(values[1]));
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Info:", "File " + fileName + " successfully added."));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Error while reading " + fileName + "!"));
        }
    }

    public static void trimQuotes(String[] values) {
        String replacement = "^\"|\"$";
        for (int i = 0; i < values.length; i++) {
            values[i] = values[i].replaceAll(replacement, "");
        }
    }

    public Event getEv() {
        return ev;
    }

    public void setEv(Event ev) {
        this.ev = ev;
    }

    public Performer getPer() {
        return per;
    }

    public void setPer(Performer per) {
        this.per = per;
    }

    public List<Performer> getPlist() {
        return plist;
    }

    public void setPlist(List<Performer> plist) {
        this.plist = plist;
    }

    public List<SocialLink> getLlist() {
        return llist;
    }

    public void setLlist(List<SocialLink> llist) {
        this.llist = llist;
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

    public String getFbLink() {
        return fbLink;
    }

    public void setFbLink(String fbLink) {
        this.fbLink = fbLink;
        //llist.add(new SocialLink(ev, "Facebook", fbLink));
    }

    public String getTwLink() {
        return twLink;
    }

    public void setTwLink(String twLink) {
        this.twLink = twLink;
        //llist.add(new SocialLink(ev, "Twitter", twLink));
    }

    public String getInstaLink() {
        return instaLink;
    }

    public void setInstaLink(String instaLink) {
        this.instaLink = instaLink;
        //llist.add(new SocialLink(ev, "Instagram", instaLink));
    }

    public String getYtLink() {
        return ytLink;
    }

    public void setYtLink(String ytlink) {
        this.ytLink = ytlink;
        //llist.add(new SocialLink(ev, "YouTube", ytLink));
    }
}
