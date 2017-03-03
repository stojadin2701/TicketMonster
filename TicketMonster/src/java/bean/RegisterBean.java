/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bean;

import dao.UserDAO;
import entity.User;
import java.io.IOException;
import javax.inject.Named;
import javax.faces.bean.SessionScoped;
import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import org.hibernate.*;
import org.hibernate.exception.ConstraintViolationException;
import util.HibernateUtil;

/**
 *
 * @author Nemanja
 */
@Named(value = "registerBean")
@ViewScoped
public class RegisterBean implements Serializable {

    private String username;
    private String eMail;
    private String password;
    private String passwordConfirmation;
    private String firstName;
    private String lastName;
    private String phone;

    private static final Pattern EMAIL_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
    private static final Pattern PASSWORD_REGEX = Pattern.compile("^[a-zA-Z](?=.{6,12}$)(?=[^A-Z]*[A-Z])(?=[^a-z]*[a-z])(?=[^0-9]*[0-9])(?:([\\w\\d*?!:;])\\1?(?!\\1))+$");

    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void register() {
        Matcher matcherEm = EMAIL_REGEX.matcher(eMail);
        Matcher matcherPass = PASSWORD_REGEX.matcher(password);
        boolean errorHappened = false;
        if (!password.equals(passwordConfirmation)) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "Password and password confirmation do not match!"));
            errorHappened = true;
        }
        if (!matcherEm.find()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "The email address isn't in the right format!"));
            errorHappened = true;
        }
        if (!matcherPass.find()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "Password isn't in the right format!"));
            errorHappened = true;
        }
        if (!errorHappened) {
            try {
                Session session = HibernateUtil.getSessionFactory().openSession();
                Transaction transaction = session.beginTransaction();
                User usr = new User();
                usr.setEMail(eMail);
                usr.setFirstName(firstName);
                usr.setLastName(lastName);
                usr.setPassword(password);
                usr.setPhone(phone);
                usr.setStatus("pending");
                usr.setUsername(username);
                session.persist(usr);
                transaction.commit();
                FacesContext.getCurrentInstance().getExternalContext().redirect("index.xhtml");
                FacesContext.getCurrentInstance().responseComplete();
            } catch (ConstraintViolationException e) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "This username or email is already taken!"));
            } catch (IOException e) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "Something bad happened!"));
            }
        }
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String geteMail() {
        return eMail;
    }

    public void seteMail(String eMail) {
        this.eMail = eMail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPasswordConfirmation() {
        return passwordConfirmation;
    }

    public void setPasswordConfirmation(String passwordConfirmation) {
        this.passwordConfirmation = passwordConfirmation;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

}
