/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bean;

import dao.EventDAO;
import dao.MessageDAO;
import dao.ReservationDAO;
import entity.*;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
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
import org.primefaces.event.SelectEvent;
import util.SearchContainer;

/**
 *
 * @author Nemanja
 */
@Named(value = "eventsBean")
@ViewScoped
public class EventsBean implements Serializable {

    private List<Event> events;
    private List<Event> foundEvents;
    private List<Event> topRated;
    private List<Event> ongoingUpcoming;
    private List<Event> mostViewed;
    private List<Event> mostVisited;
    private SearchContainer search;
    private boolean searchHappened;

    @ManagedProperty(value = "#{loginBean}")
    private LoginBean loginBean;

    public LoginBean getLoginBean() {
        return loginBean;
    }

    public void setLoginBean(LoginBean loginBean) {
        this.loginBean = loginBean;
    }

    public void cancelEvent(Event ev) {
        if (ev != null) {
            EventDAO.cancel(ev);
            ev.getReservations().forEach(res -> MessageDAO.addFromReservation((Reservation) res, MessageDAO.EVENT_CANCELLED));
            ev.getReservations().forEach(res -> ReservationDAO.setEventCancelled((Reservation) res));
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Info:", "Event " + ev.getEventName() + " cancelled."));
            events.remove(ev);
        }
    }

    public void editEvent(Event ev) {
        if (ev != null) {
            try {
                FacesContext context = FacesContext.getCurrentInstance();
                loginBean = context.getApplication().evaluateExpressionGet(context, "#{loginBean}", LoginBean.class);
                loginBean.setCurrentEvent(ev);
                //FacesContext.getCurrentInstance().getExternalContext().getFlash().put("event", ev);
                //FacesContext.getCurrentInstance().getExternalContext().getRequestMap().put("event", ev);
                FacesContext.getCurrentInstance().getExternalContext().redirect("edit_event.xhtml");
                FacesContext.getCurrentInstance().responseComplete();
            } catch (IOException ex) {
                Logger.getLogger(EventsBean.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void showDetails(Event ev) {
        if (ev != null) {
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

    public EventsBean() {
        events = EventDAO.getAll();
        topRated = EventDAO.topRated();
        ongoingUpcoming = EventDAO.ongoingUpcoming();
        mostViewed = EventDAO.mostViewed();
        mostVisited = EventDAO.mostVisited();
        search = new SearchContainer();
    }
  
    public void searchEvents() {
        //foundEvents = EventDAO.search(search);
        events = EventDAO.search(search);
        //searchHappened=true;
        search = new SearchContainer();
    }

    public void getEventsToday(SelectEvent event) {
        events = EventDAO.getByDate((Date) event.getObject());
    }

    public List<Event> getTopRated() {
        return topRated;
    }

    public void setTopRated(List<Event> topRated) {
        this.topRated = topRated;
    }

    public List<Event> getOngoingUpcoming() {
        return ongoingUpcoming;
    }

    public void setOngoingUpcoming(List<Event> ongoingUpcoming) {
        this.ongoingUpcoming = ongoingUpcoming;
    }

    public List<Event> getMostViewed() {
        return mostViewed;
    }

    public void setMostViewed(List<Event> mostViewed) {
        this.mostViewed = mostViewed;
    }

    public List<Event> getMostVisited() {
        return mostVisited;
    }

    public void setMostVisited(List<Event> mostVisited) {
        this.mostVisited = mostVisited;
    }

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

    public SearchContainer getSearch() {
        return search;
    }

    public void setSearch(SearchContainer search) {
        this.search = search;
    }

    public List<Event> getFoundEvents() {
        return foundEvents;
    }

    public void setFoundEvents(List<Event> foundEvents) {
        this.foundEvents = foundEvents;
    }

    public boolean isSearchHappened() {
        return searchHappened;
    }

    public void setSearchHappened(boolean searchHappened) {
        this.searchHappened = searchHappened;
    }

}
