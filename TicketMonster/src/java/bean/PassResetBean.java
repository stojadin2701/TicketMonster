/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bean;

import dao.UserDAO;
import entity.User;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.faces.view.ViewScoped;

/**
 *
 * @author Nemanja
 */
@Named(value = "passResetBean")
@ViewScoped
public class PassResetBean implements Serializable {

    private String oldPassword;
    private String newPassword;
    private String newPasswordConfirm;

    public String resetPassword(String username) {
        if (oldPassword == null || newPassword == null || newPasswordConfirm == null || username == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "You must fill all the required fields!"));
            return "pass_change";
        }
        if (newPassword.equals(oldPassword)) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "New password and old password cannot be same!"));
            return "pass_change";
        }

        if (!newPassword.equals(newPasswordConfirm)) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "New password and it's confirmation do not match!"));
            return "pass_change";
        }

        User usr = UserDAO.getByUsernamePass(username, oldPassword);

        if (usr == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Current password you have entered is invalid!"));
            return "pass_change";
        }

        UserDAO.changePassword(usr, newPassword);
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "You have succesfuly changed your password."));
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "login";
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getNewPasswordConfirm() {
        return newPasswordConfirm;
    }

    public void setNewPasswordConfirm(String newPasswordConfirm) {
        this.newPasswordConfirm = newPasswordConfirm;
    }

}
