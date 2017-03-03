/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import static dao.EventDAO.session;
import static dao.UserDAO.session;
import static dao.VideoDAO.session;
import static dao.VideoDAO.store;
import entity.Image;
import entity.User;
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
public class ImageDAO {

    static Session session = null;

    public static void store(Image img) {
        session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        session.persist(img);
        transaction.commit();
        if (session != null && session.isOpen()) {
            session.close();
        }
    }

    public static void storeMultiple(List<Image> ilist) {
        for (Image img : ilist) {
            store(img);
        }
    }

    public static void approveImage(Image img, boolean approve) {
        if (img != null) {
            session = HibernateUtil.getSessionFactory().openSession();
            Transaction transaction = session.beginTransaction();
            if (approve) {
                img.setApproved(true);
                session.update(img);
            }
            else{
               try {
                    Path deletePath = Paths.get(img.getImageUrl());
                    Files.delete(deletePath);                
                    session.delete(img);
                } catch (IOException ex) {
                    Logger.getLogger(ImageDAO.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            transaction.commit();
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    public static List<Image> getUnapproved() {
        session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();

        List<Image> imgList = null;

        Query query = session.createQuery("from Image i join fetch i.event where i.approved=0");
        try {

            imgList = query.list();
            transaction.commit();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            transaction.rollback();
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return imgList;
    }

    public static void removeMultiple(List<Image> removedImageList) {
        session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        for (Image image : removedImageList) {
            try {
                Path deletePath = Paths.get(image.getImageUrl());
                Files.delete(deletePath);
                session.delete(image);
            } catch (IOException ex) {
                Logger.getLogger(ImageDAO.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        transaction.commit();
        if (session != null && session.isOpen()) {
            session.close();
        }
    }

    public static void saveOrUpdateMultiple(List<Image> ilist) {
        session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        for (Image image : ilist) {
            session.saveOrUpdate(image);
        }
        transaction.commit();
        if (session != null && session.isOpen()) {
            session.close();
        }
    }

}
