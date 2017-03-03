/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bean;

import com.fasterxml.jackson.core.JsonFactory;
import dao.MessageDAO;
import entity.User;
import javax.inject.Named;
import javax.faces.bean.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import dao.UserDAO;
import entity.Event;
import entity.Message;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import java.security.GeneralSecurityException;

/**
 *
 * @author Nemanja
 */
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import org.primefaces.context.RequestContext;

@ManagedBean
@SessionScoped
public class LoginBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean isAdmin = false;

    private boolean loggedIn = false;

    private boolean gLogin = false;

    public boolean isgLogin() {
        return gLogin;
    }

    public void setgLogin(boolean gLogin) {
        this.gLogin = gLogin;
    }

    private String message;

    private Event currentEvent;

    private User currentUser;

    private List<Message> messages = new ArrayList<>();

    public boolean hasMessages() {
        return !messages.isEmpty();
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public Event getCurrentEvent() {
        return currentEvent;
    }

    public void setCurrentEvent(Event currentEvent) {
        this.currentEvent = currentEvent;
    }

    public boolean isUser() {
        return loggedIn && !isAdmin;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    private String username;

    public boolean getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String login() {
        User usr = UserDAO.getByUsernamePass(username, password);
        //     try {
        if (usr == null) {
            loggedIn = false;
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "Invalid username or password, please try again."));
            // FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Invalid Login!", "Please Try Again!"));
            //             FacesContext.getCurrentInstance().getExternalContext().redirect("index.xhtml?faces-redirect=true");
            return "login";
        }

        if (usr.getStatus().equals("pending")) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "The administrator has not yet approved your account, please try again tomorrow."));
            return "login";
        } else if (usr.getStatus().equals("blocked")) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "You have been banned from this website!"));
            return "login";

        } else if (usr.getStatus().equals("refused")) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "Your registration has been refused!"));
            return "login";
        } else if (usr.isAdmin() == true) {
            isAdmin = true;
            loggedIn = true;
            currentUser = usr;
            UserDAO.logDateTime(usr);
            //            FacesContext.getCurrentInstance().getExternalContext().redirect("admin/admin.xhtml?faces-redirect=true");
            return "index";
        } else {
            loggedIn = true;
            currentUser = usr;
            UserDAO.logDateTime(usr);

            messages = MessageDAO.getForUserAndDelete(usr);
            for (Message msg : messages) {
                switch (msg.getType()) {
                    case "reserved":
                        FacesContext.getCurrentInstance().addMessage("userNotifications", new FacesMessage(FacesMessage.SEVERITY_WARN, "Notification:", "The event you have reserved a ticket for: " + msg.getEvent().getEventName() + " has been cancelled."));
                        break;
                    case "sold":
                        FacesContext.getCurrentInstance().addMessage("userNotifications", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Notification:", "The event you have bought a ticket for: " + msg.getEvent().getEventName() + " has been cancelled."));
                        break;
                    case "performer":
                        FacesContext.getCurrentInstance().addMessage("userNotifications", new FacesMessage(FacesMessage.SEVERITY_WARN, "Notification:", "The event you have a ticket for: " + msg.getEvent().getEventName() + " has changed it's lineup."));
                        break;
                    case "timetable":
                        FacesContext.getCurrentInstance().addMessage("userNotifications", new FacesMessage(FacesMessage.SEVERITY_WARN, "Notification:", "The event you have a ticket for: " + msg.getEvent().getEventName() + " has changed it's timetable."));
                        break;
                }
            }
            return "index";
        }
        //     } catch (IOException ex) {
        //         Logger.getLogger(LoginBean.class.getName()).log(Level.SEVERE, null, ex);
        //     }
    }

    /*    public void googleLogin() {
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String tokenId = params.get("token");
        String email = params.get("email");
        String firstName = params.get("first_name");
        String lastName = params.get("last_name");

        User usr = UserDAO.findByGEmail(email);
        if (usr == null) {
            usr = UserDAO.registerG(tokenId, email, firstName, lastName);
        }
        if (usr != null) {
            loggedIn = true;
            currentUser = usr;
            username = currentUser.getUsername();
            gLogin = true;
            UserDAO.logDateTime(usr);
            messages = MessageDAO.getForUserAndDelete(usr);
            for (Message msg : messages) {
                switch (msg.getType()) {
                    case "reserved":
                        FacesContext.getCurrentInstance().addMessage("userNotifications", new FacesMessage(FacesMessage.SEVERITY_WARN, "Notification:", "The event you have reserved a ticket for: " + msg.getEvent().getEventName() + " has been cancelled."));
                        break;
                    case "sold":
                        FacesContext.getCurrentInstance().addMessage("userNotifications", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Notification:", "The event you have bought a ticket for: " + msg.getEvent().getEventName() + " has been cancelled."));
                        break;
                    case "performer":
                        FacesContext.getCurrentInstance().addMessage("userNotifications", new FacesMessage(FacesMessage.SEVERITY_WARN, "Notification:", "The event you have a ticket for: " + msg.getEvent().getEventName() + " has changed it's lineup."));
                        break;
                    case "timetable":
                        FacesContext.getCurrentInstance().addMessage("userNotifications", new FacesMessage(FacesMessage.SEVERITY_WARN, "Notification:", "The event you have a ticket for: " + msg.getEvent().getEventName() + " has changed it's timetable."));
                        break;
                }
            }
            try {
                FacesContext.getCurrentInstance().getExternalContext().redirect("index.xhtml");
                FacesContext.getCurrentInstance().responseComplete();
            } catch (IOException ex) {
                Logger.getLogger(LoginBean.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "Your email already exists in the system, try logging in without google!"));
        }
    }
*/
    public String logout() {
        currentUser = null;
        currentEvent = null;
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        if (gLogin) {
            RequestContext requestContext = RequestContext.getCurrentInstance();
            requestContext.execute("signOut()");
        }
        return "index";
    }
     
    public void googleLogin() {

        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String tokenId = params.get("token");
        String email = params.get("email");
        String firstName = params.get("first_name");
        String lastName = params.get("last_name");

        try {
            GsonFactory jsonFactory = new GsonFactory();
            NetHttpTransport transport = new NetHttpTransport();
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory).setAudience(Collections.singletonList("640461808271-qlamo36cn4t9c2k6s4ujggf96smmvhfh.apps.googleusercontent.com")).build();

            GoogleIdToken idToken = verifier.verify(tokenId);

            if (idToken != null) {
                User usr = UserDAO.findByGEmail(email);
                if (usr == null) {
                    usr = UserDAO.registerG(email, firstName, lastName);
                }
                if (usr != null) {
                    loggedIn = true;
                    currentUser = usr;
                    username = currentUser.getUsername();
                    gLogin = true;
                    UserDAO.logDateTime(usr);
                    messages = MessageDAO.getForUserAndDelete(usr);
                    for (Message msg : messages) {
                        switch (msg.getType()) {
                            case "reserved":
                                FacesContext.getCurrentInstance().addMessage("userNotifications", new FacesMessage(FacesMessage.SEVERITY_WARN, "Notification:", "The event you have reserved a ticket for: " + msg.getEvent().getEventName() + " has been cancelled."));
                                break;
                            case "sold":
                                FacesContext.getCurrentInstance().addMessage("userNotifications", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Notification:", "The event you have bought a ticket for: " + msg.getEvent().getEventName() + " has been cancelled."));
                                break;
                            case "performer":
                                FacesContext.getCurrentInstance().addMessage("userNotifications", new FacesMessage(FacesMessage.SEVERITY_WARN, "Notification:", "The event you have a ticket for: " + msg.getEvent().getEventName() + " has changed it's lineup."));
                                break;
                            case "timetable":
                                FacesContext.getCurrentInstance().addMessage("userNotifications", new FacesMessage(FacesMessage.SEVERITY_WARN, "Notification:", "The event you have a ticket for: " + msg.getEvent().getEventName() + " has changed it's timetable."));
                                break;
                        }
                    }
                    try {
                        FacesContext.getCurrentInstance().getExternalContext().redirect("index.xhtml");
                        FacesContext.getCurrentInstance().responseComplete();
                    } catch (IOException ex) {
                        Logger.getLogger(LoginBean.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "Your email already exists in the system, try logging in without google!"));
                }
            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "There has been an error with your token validation!"));
            }
        } catch (GeneralSecurityException | IOException ex) {
            Logger.getLogger(LoginBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
