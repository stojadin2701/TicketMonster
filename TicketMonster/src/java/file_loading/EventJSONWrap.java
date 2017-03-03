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
public class EventJSONWrap {
    @SerializedName("Festival")
    @Expose
    private EventJSON evJSON;

    public EventJSON getEvJSON() {
        return evJSON;
    }

    public void setEvJSON(EventJSON evJSON) {
        this.evJSON = evJSON;
    }
}
