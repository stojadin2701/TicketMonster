/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import static dao.EventDAO.session;
import static dao.PerformerDAO.store;
import entity.Performer;
import entity.SocialLink;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.Transaction;
import util.HibernateUtil;

/**
 *
 * @author Nemanja
 */
public class SocialLinkDAO {

    static Session session = null;

    public static void store(SocialLink slink) {
        session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        session.persist(slink);
        transaction.commit();
        if (session != null && session.isOpen()) {
            session.close();
        }
    }

    public static void storeMultiple(List<SocialLink> llist) {
        for (SocialLink slink : llist) {
            store(slink);
        }
    }

    public static void update(SocialLink slink) {
        session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        session.update(slink);
        transaction.commit();
        if (session != null && session.isOpen()) {
            session.close();
        }
    }

    public static void delete(SocialLink slink) {
        session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        session.delete(slink);
        transaction.commit();
        if (session != null && session.isOpen()) {
            session.close();
        }
    }
}
