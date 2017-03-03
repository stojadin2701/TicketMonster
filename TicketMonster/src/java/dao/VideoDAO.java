/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import static dao.ImageDAO.session;
import static dao.SocialLinkDAO.session;
import entity.Image;
import entity.Video;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import util.HibernateUtil;

/**
 *
 * @author Nemanja
 */
public class VideoDAO {

    static Session session = null;

    public static void store(Video vid) {
        session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        session.persist(vid);
        transaction.commit();
        if (session != null && session.isOpen()) {
            session.close();
        }
    }

    public static void storeMultiple(List<Video> vlist) {
        for (Video vid : vlist) {
            store(vid);
        }
    }

    public static void approveVideo(Video vid, boolean approve) {
        if (vid != null) {
            session = HibernateUtil.getSessionFactory().openSession();
            Transaction transaction = session.beginTransaction();
            if (approve) {
                vid.setApproved(true);
                session.update(vid);
            } else {
                try {
                    Path deletePath = Paths.get(vid.getVideoUrl());
                    Files.delete(deletePath);                
                    session.delete(vid);
                } catch (IOException ex) {
                    Logger.getLogger(VideoDAO.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            transaction.commit();
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    public static List<Video> getUnapproved() {
        session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();

        List<Video> vidList = null;

        Query query = session.createQuery("from Video v join fetch v.event where v.approved=0");
        try {

            vidList = query.list();
            transaction.commit();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            transaction.rollback();
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return vidList;
    }

    public static void removeMultiple(List<Video> removedVideoList) {
        session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        for (Video video : removedVideoList) {
            try {
                Path deletePath = Paths.get("C:/PIA_WEB"+video.getVideoUrl());
                Files.delete(deletePath);
                session.delete(video);
            } catch (IOException ex) {
                Logger.getLogger(VideoDAO.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        transaction.commit();
        if (session != null && session.isOpen()) {
            session.close();
        }
    }

    public static void saveOrUpdateMultiple(List<Video> vlist) {
        session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        for (Video video : vlist) {
            session.saveOrUpdate(video);
        }
        transaction.commit();
        if (session != null && session.isOpen()) {
            session.close();
        }
    }
}
