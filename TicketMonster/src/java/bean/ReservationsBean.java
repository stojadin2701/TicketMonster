/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bean;

import dao.ReservationDAO;
import dao.UserDAO;
import entity.Event;
import entity.Reservation;
import entity.User;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.faces.view.ViewScoped;

/**
 *
 * @author Nemanja
 */
@Named(value = "reservationsBean")
@ViewScoped
public class ReservationsBean implements Serializable {

    List<Reservation> pendingReservations;

    public ReservationsBean() {
        pendingReservations = ReservationDAO.getPending();
    }

    public List<Reservation> getPendingReservations() {
        return pendingReservations;
    }
    
    @ManagedProperty(value = "#{loginBean}")
    private LoginBean loginBean;

    public LoginBean getLoginBean() {
        return loginBean;
    }

    public void setLoginBean(LoginBean loginBean) {
        this.loginBean = loginBean;
    }

    public void setPendingReservations(List<Reservation> pendingReservations) {
        this.pendingReservations = pendingReservations;
    }

    public void sellReservation(Reservation res) {
        if (res != null && "pending".equals(res.getStatus())) {
            ReservationDAO.changeStatus(res, "sold");
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Info:", "Reservation sold."));
            pendingReservations.remove(res);
        }
        else{
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "You cannot sell a non-pending reservation."));
        }
    }

    public List<Reservation> getReservationsForUser(User u) {
        if (u != null) {
            return ReservationDAO.getReservationsForUser(u);
        }
        return new ArrayList<>();
    }

    public void cancelReservation(Reservation res) {
        if (res != null && "pending".equals(res.getStatus())) {
            ReservationDAO.changeStatus(res, "cancelled_by_user");
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Info:", "Reservation cancelled."));
        }
        else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "You cannot cancel a non-pending reservation."));
        }
    }
    
    public void showDetails(Event ev) {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            loginBean = context.getApplication().evaluateExpressionGet(context, "#{loginBean}", LoginBean.class);
            loginBean.setCurrentEvent(ev);
            //FacesContext.getCurrentInstance().getExternalContext().getFlash().put("event", ev);
            //FacesContext.getCurrentInstance().getExternalContext().getRequestMap().put("event", ev);
            FacesContext.getCurrentInstance().getExternalContext().redirect("event_details.xhtml");
            FacesContext.getCurrentInstance().responseComplete();
        } catch (IOException ex) {
            Logger.getLogger(EventsBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
