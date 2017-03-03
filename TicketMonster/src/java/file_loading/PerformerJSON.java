/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package file_loading;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 *
 * @author Nemanja
 */
public class PerformerJSON {

    @SerializedName("Performer")
    @Expose
    private String performerName;

    @SerializedName("StartDate")
    @Expose
    private String startDate;

    @SerializedName("EndDate")
    @Expose
    private String endDate;

    @SerializedName("StartTime")
    @Expose
    private String startTime;
    
    @SerializedName("EndTime")
    @Expose
    private String endTime;

    public String getPerformerName() {
        return performerName;
    }

    public void setPerformerName(String performerName) {
        this.performerName = performerName;
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

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    
}
