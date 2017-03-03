/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bean;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Named;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 *
 * @author Nemanja
 */
@Named(value = "imageFetcherBean")
@ApplicationScoped
public class ImageFetcherBean {

    public static final Map<String,String> mimeLookupTable = new HashMap<>();
    static{
        mimeLookupTable.put("jpg", "image/jpeg");
        mimeLookupTable.put("jpeg", "image/jpeg");
        mimeLookupTable.put("png", "image/png");
        mimeLookupTable.put("gif", "image/gif");
    }
 
    public StreamedContent getImage() throws IOException {
        FacesContext context = FacesContext.getCurrentInstance();
 
        if (context.getCurrentPhaseId() == PhaseId.RENDER_RESPONSE) {
            return new DefaultStreamedContent();
        }
        else {
            String filename = context.getExternalContext().getRequestParameterMap().get("filename");
            return new DefaultStreamedContent(new FileInputStream(new File(filename)), mimeLookupTable.get(filename.substring(filename.lastIndexOf('.')+1)));
        }
    }
   
    public StreamedContent getVideo() throws IOException {
        FacesContext context = FacesContext.getCurrentInstance();
 
        if (context.getCurrentPhaseId() == PhaseId.RENDER_RESPONSE) {
            return new DefaultStreamedContent();
        }
        else {
            String filename = context.getExternalContext().getRequestParameterMap().get("filename");
            return new DefaultStreamedContent(new FileInputStream(new File(filename)), "application/x-troff-msvideo");
        }
    }
    
}
