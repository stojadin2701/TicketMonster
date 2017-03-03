/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bean;

import dao.EventDAO;
import dao.ReservationDAO;
import dao.TicketsRemainingDAO;
import java.io.Serializable;
import javax.inject.Named;
import javax.faces.view.ViewScoped;
import entity.Event;
import java.util.*;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

/**
 *
 * @author Nemanja
 */
@Named(value = "directSellingBean")
@ViewScoped
public class DirectSellingBean implements Serializable {

    List<Event> sellableEvents = null;

    private Integer daySelected;

    public Integer getDaySelected() {
        return daySelected;
    }

    public void setDaySelected(Integer daySelected) {
        this.daySelected = daySelected;
    }

    private Event selectedEvent;

    private List<Integer> availableDays;

    public DirectSellingBean() {
        sellableEvents = EventDAO.getSellable();
    }

    public List<Event> getSellableEvents() {
        return sellableEvents;
    }

    public void setSellableEvents(List<Event> sellableEvents) {
        this.sellableEvents = sellableEvents;
    }

    public void sellAll(Event ev) {
        if ((new Date()).after(ev.getEndDate())) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "You cannot reserve a ticket for an event that has ended."));
        } else if (!TicketsRemainingDAO.canBuyAll(ev)) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "Some of the days do not have tickets available."));
        } else {
            TicketsRemainingDAO.reduceAll(ev);
            EventDAO.increaseSellCount(ev, false);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Info:", "You have succesfuly sold an all-days ticket for " + ev.getEventName() + "."));
        }
    }

    public void sellOne(Event ev) {
        if (daySelected == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "You must choose a day."));
        } else {
            boolean ok = false;
            for (Integer dayNum : availableDays) {
                if (dayNum.intValue() == daySelected) {
                    ok = true;
                    break;
                }
            }
            if (!ok) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "You must choose a day which has remaining tickets."));
            } else {
                TicketsRemainingDAO.reduceOne(ev, daySelected);
                EventDAO.increaseSellCount(ev, true);
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Info:", "You have succesfuly sold a one-day ticket for " + ev.getEventName() + "."));
            }
            daySelected=null;
        }
    }

    public List<Integer> getAvailableDays() {
        return availableDays;
    }

    public List<Integer> getAvailableDays(Event ev) {
        if (ev != null) {
            availableDays = TicketsRemainingDAO.getAvailableDays(ev);
            return availableDays;
        } else {
            return new ArrayList<>();
        }

    }

    public void setAvailableDays(List<Integer> availableDays) {
        this.availableDays = availableDays;
    }

    public Event getSelectedEvent() {
        return selectedEvent;
    }

    public void setSelectedEvent(Event selectedEvent) {
        this.selectedEvent = selectedEvent;
    }

}
