/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import static dao.EventDAO.session;
import static dao.ImageDAO.session;
import static dao.ImageDAO.store;
import entity.*;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.Transaction;
import util.HibernateUtil;

/**
 *
 * @author Nemanja
 */
public class PerformerDAO {
    static Session session =null;
    
    public static void store(Performer per){
        session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        session.persist(per);
        transaction.commit();
        if (session != null && session.isOpen()) {
                session.close();
        }
    }
    
    public static void storeMultiple(List<Performer> plist){
        for (Performer per : plist) store(per);
    }

    public static void removeMultiple(List<Performer> removedPerformerList) {
        session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        for (Performer performer : removedPerformerList) {
            session.delete(performer);
        }
        transaction.commit();
        if (session != null && session.isOpen()) {
                session.close();
        }
    }

    public static void saveOrUpdateMultiple(List<Performer> plist) {
        session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        for (Performer performer : plist) {
            session.saveOrUpdate(performer);
        }
        transaction.commit();
        if (session != null && session.isOpen()) {
            session.close();
        }
    }
}
