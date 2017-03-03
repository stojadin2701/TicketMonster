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
import javax.inject.Named;
import javax.faces.view.ViewScoped;

/**
 *
 * @author Nemanja
 */
@Named(value = "activityBean")
@ViewScoped
public class ActivityBean implements Serializable  {

    List<User> recentlyActiveList;
    
    public ActivityBean() {
        recentlyActiveList = UserDAO.getRecentlyActive();
    }

    public List<User> getRecentlyActiveList() {
        return recentlyActiveList;
    }

    public void setRecentlyActiveList(List<User> recentlyActiveList) {
        this.recentlyActiveList = recentlyActiveList;
    }
    
}
