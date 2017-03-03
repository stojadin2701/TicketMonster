/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bean;

import dao.UserDAO;
import entity.User;
import java.io.Serializable;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.faces.view.ViewScoped;

/**
 *
 * @author Nemanja
 */
@Named(value = "userApprovalBean")
@ViewScoped
public class UserApprovalBean implements Serializable {

    List<User> userApprovalList;

    public UserApprovalBean() {
        userApprovalList = UserDAO.getUnapproved();
    }

    public void approveUser(User u, boolean approve) {
        if (u != null) {
            UserDAO.manageRegistration(u, approve);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Info:", u.getUsername() + (approve?" approved.":" rejected.")));
            userApprovalList.remove(u);
        }
    }

    public List<User> getUserApprovalList() {
        return userApprovalList;
    }

    public void setUserApprovalList(List<User> userApprovalList) {
        this.userApprovalList = userApprovalList;
    }

}
