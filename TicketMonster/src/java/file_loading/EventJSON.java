/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package file_loading;

import com.google.gson.annotations.*;
import java.util.List;

/**
 *
 * @author Nemanja
 */
public class EventJSON {

    @SerializedName("Name")
    @Expose
    private String eventName;

    @SerializedName("Place")
    @Expose
    private String eventPlace;

    @SerializedName("StartDate")
    @Expose
    private String startDate;

    @SerializedName("EndDate")
    @Expose
    private String endDate;

    @SerializedName("Tickets")
    @Expose
    private List<String> ticketPrices;

    @SerializedName("PerformersList")
    @Expose
    private List<PerformerJSON> performers;

    @SerializedName("SocialNetworks")
    @Expose
    private List<SocialLinkJSON> socialLinks;

    @SerializedName("MaxReservationsPerUser")
    @Expose
    private String maxReservationsPerUser;

    @SerializedName("MaxTicketsPerDay")
    @Expose
    private String maxTicketsAvailablePerDay;

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventPlace() {
        return eventPlace;
    }

    public void setEventPlace(String eventPlace) {
        this.eventPlace = eventPlace;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public List<String> getTicketPrices() {
        return ticketPrices;
    }

    public void setTicketPrices(List<String> ticketPrices) {
        this.ticketPrices = ticketPrices;
    }

    public List<PerformerJSON> getPerformers() {
        return performers;
    }

    public void setPerformers(List<PerformerJSON> performers) {
        this.performers = performers;
    }

    public List<SocialLinkJSON> getSocialLinks() {
        return socialLinks;
    }

    public void setSocialLinks(List<SocialLinkJSON> socialLinks) {
        this.socialLinks = socialLinks;
    }

    public String getMaxReservationsPerUser() {
        return maxReservationsPerUser;
    }

    public void setMaxReservationsPerUser(String maxReservationsPerUser) {
        this.maxReservationsPerUser = maxReservationsPerUser;
    }

    public String getMaxTicketsAvailablePerDay() {
        return maxTicketsAvailablePerDay;
    }

    public void setMaxTicketsAvailablePerDay(String maxTicketsAvailablePerDay) {
        this.maxTicketsAvailablePerDay = maxTicketsAvailablePerDay;
    }

    
}
