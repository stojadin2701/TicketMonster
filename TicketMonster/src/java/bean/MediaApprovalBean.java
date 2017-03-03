/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bean;

import dao.ImageDAO;
import dao.VideoDAO;
import entity.Image;
import entity.Video;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.faces.view.ViewScoped;

/**
 *
 * @author Nemanja
 */
@Named(value = "mediaApprovalBean")
@ViewScoped
public class MediaApprovalBean implements Serializable {

    List<Image> imgApprovalList;
    List<Video> vidApprovalList;

    public MediaApprovalBean() {
        imgApprovalList = ImageDAO.getUnapproved();
        vidApprovalList = VideoDAO.getUnapproved();
    }

    public void approveImage(Image img, boolean approve) {
        if (img != null) {
            ImageDAO.approveImage(img, approve);
            Path p = Paths.get(img.getImageUrl());
            String imgName = p.getFileName().toString();
            FacesContext.getCurrentInstance().addMessage("imageApproval", new FacesMessage(FacesMessage.SEVERITY_INFO, "Info:", imgName + (approve ? " approved." : " rejected.")));
            imgApprovalList.remove(img);
        }
    }
    
    public void approveVideo(Video vid, boolean approve) {
        if (vid != null) {
            VideoDAO.approveVideo(vid, approve);
            Path p = Paths.get(vid.getVideoUrl());
            String vidName = p.getFileName().toString();
            FacesContext.getCurrentInstance().addMessage("videoApproval", new FacesMessage(FacesMessage.SEVERITY_INFO, "Info:", vidName + (approve ? " approved." : " rejected.")));
            vidApprovalList.remove(vid);
        }
    }

    public List<Image> getImgApprovalList() {
        return imgApprovalList;
    }

    public void setImgApprovalList(List<Image> imgApprovalList) {
        this.imgApprovalList = imgApprovalList;
    }

    public List<Video> getVidApprovalList() {
        return vidApprovalList;
    }

    public void setVidApprovalList(List<Video> vidApprovalList) {
        this.vidApprovalList = vidApprovalList;
    }

    

}
