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
import java.util.*;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.primefaces.event.FileUploadEvent;

/**
 *
 * @author Nemanja
 */
@Named(value = "addFestivalBean")
@ViewScoped
public class AddFestivalBean implements Serializable {

    private Event ev = new Event();
    private Performer per = new Performer();

    private List<Performer> plist = new ArrayList<>();
    private List<SocialLink> llist = new ArrayList<>();
    private List<Image> ilist = new ArrayList<>();
    private List<Video> vlist = new ArrayList<>();

    private String fbLink = null;
    private String twLink = null;
    private String instaLink = null;
    private String ytLink = null;

    private final Date currentDate = new Date();

    private boolean fileAdded = false;
    
    String regex = "((http|https)://)?(www[.])?facebook.com/.+";
    
    private static final Pattern FB_REGEX = Pattern.compile("((http|https)://)?(www[.])?facebook.com/.+");
    private static final Pattern TW_REGEX = Pattern.compile("((http|https)://)?(www[.])?twitter.com/.+");
    private static final Pattern INSTA_REGEX = Pattern.compile("((http|https)://)?(www[.])?instagram.com/.+");
    private static final Pattern YT_REGEX = Pattern.compile("((http|https)://)?(www[.])?youtube.com/.+");
    
    
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

    public AddFestivalBean() {
        ev.setStartDate(currentDate);
    }

    public void removePerformer(Performer p) {
        if (p != null) {
            plist.remove(p);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Info:", p.getPerformerName() + " removed from the event."));
        }
    }

    public void store() {
        EventDAO.store(ev);

        SocialLinkDAO.storeMultiple(llist);
        ImageDAO.storeMultiple(ilist);
        VideoDAO.storeMultiple(vlist);
        PerformerDAO.storeMultiple(plist);

        TicketsRemainingDAO.addTicketsRemainingFromEvent(ev);

        FacesContext.getCurrentInstance().addMessage("adminNotifications", new FacesMessage(FacesMessage.SEVERITY_INFO, "Info:", "Event added"));
        try {
            FacesContext.getCurrentInstance().getExternalContext().redirect("index.xhtml");
            FacesContext.getCurrentInstance().responseComplete();
        } catch (IOException ex) {
            Logger.getLogger(AddFestivalBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String onFlowProcess(FlowEvent event) {
        if (event.getOldStep().equals("add_file")) {
            if (fileAdded) {
                return event.getNewStep();
            } else {
                FacesContext.getCurrentInstance().addMessage("adminNotifications", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "You must upload a file!"));
                return event.getOldStep();
            }
        } else if (event.getOldStep().equals("event_info")) {
            if (ev.getOneDayTicketPrice() > 0 && ev.getAllDaysTicketPrice() > 0 && ev.getMaxReservationsPerUser() > 0 && ev.getMaxTicketsAvailablePerDay() > 0) {
                return event.getNewStep();
            } else {
                FacesContext.getCurrentInstance().addMessage("adminNotifications", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "Number values on this page must be positive!"));
                return event.getOldStep();
            }
        }
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

    public void removeImage(Image img) {
        try {
            Path deletePath = Paths.get(img.getImageUrl());
            Files.delete(deletePath);
            ilist.remove(img);
        } catch (IOException ex) {
            Logger.getLogger(AddFestivalBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void removeVideo(Video vid) {
        try {
            Path deletePath = Paths.get(vid.getVideoUrl());
            Files.delete(deletePath);
            vlist.remove(vid);
        } catch (IOException ex) {
            Logger.getLogger(AddFestivalBean.class.getName()).log(Level.SEVERE, null, ex);
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
                    fileAdded = true;
                } else {
                    readCSV(filePath.toAbsolutePath().toString(), event.getFile().getFileName());
                    fileAdded = true;
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
        llist.add(new SocialLink(ev, "Facebook", fbLink));
    }

    public String getTwLink() {
        return twLink;
    }

    public void setTwLink(String twLink) {
        this.twLink = twLink;
        llist.add(new SocialLink(ev, "Twitter", twLink));
    }

    public String getInstaLink() {
        return instaLink;
    }

    public void setInstaLink(String instaLink) {
        this.instaLink = instaLink;
        llist.add(new SocialLink(ev, "Instagram", instaLink));
    }

    public String getYtLink() {
        return ytLink;
    }

    public void setYtLink(String ytlink) {
        this.ytLink = ytlink;
        llist.add(new SocialLink(ev, "YouTube", ytLink));
    }
}
