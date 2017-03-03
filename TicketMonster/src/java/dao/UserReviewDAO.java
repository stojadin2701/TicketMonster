/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import static dao.UserDAO.session;
import entity.User;
import entity.UserReview;
import java.util.List;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import util.HibernateUtil;

/**
 *
 * @author Nemanja
 */
public class UserReviewDAO {

    static Session session = null;

    public static List<UserReview> getForEventName(String eventName) {
        List<UserReview> urList = null;
        session = HibernateUtil.getSessionFactory().openSession();
        Query query = session.createQuery("from UserReview ur join fetch ur.user where ur.eventName=:ev_name order by ur.reviewTime desc");
        try {
            query.setParameter("ev_name", eventName);
            urList = query.list();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return urList;
    }

    public static void store(UserReview userReview) {
        session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        session.persist(userReview);
        transaction.commit();
        if (session != null && session.isOpen()) {
            session.close();
        }
    }

}
