/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import static dao.PerformerDAO.session;
import util.HibernateUtil;
import entity.User;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import org.hibernate.*;
import org.hibernate.exception.ConstraintViolationException;

/**
 *
 * @author Nemanja
 */
public class UserDAO {

    static Session session = null;

    public static User getByUsernamePass(String username, String password) {
        session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        User usr = null;
        Query query = session.createQuery("from User where username = :username and password = :password ");
        try {
            query.setParameter("username", username);
            query.setParameter("password", password);
            usr = (User) query.list().get(0);
            transaction.commit();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            transaction.rollback();
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return usr;
    }

    public static void logDateTime(User u) {
        if (u != null) {
            session = HibernateUtil.getSessionFactory().openSession();
            Transaction transaction = session.beginTransaction();
            u.setLastLoginTime(new Date());
            session.update(u);
            transaction.commit();
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    public static void manageRegistration(User u, boolean approve) {
        if (u != null) {
            session = HibernateUtil.getSessionFactory().openSession();
            Transaction transaction = session.beginTransaction();
            u.setStatus(approve ? "regular" : "refused");
            session.update(u);
            transaction.commit();
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    public static List<User> getRecentlyActive() {
        List<User> userList = null;
        session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();

        Query query = session.createQuery("from User where last_login_time is not null order by last_login_time desc");
        try {
            query.setFirstResult(0);
            query.setMaxResults(10);
            userList = query.list();
            transaction.commit();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            transaction.rollback();
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return userList;
    }

    public static List<User> getUnapproved() {
        session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();

        List<User> userList = null;

        Query query = session.createQuery("from User where status='pending'");
        try {

            userList = query.list();
            transaction.commit();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            transaction.rollback();
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return userList;
    }

    public static boolean changePassword(User u, String password) {
        if (u != null && password != null) {
            session = HibernateUtil.getSessionFactory().openSession();
            Transaction transaction = session.beginTransaction();
            u.setPassword(password);
            session.update(u);
            transaction.commit();
            if (session != null && session.isOpen()) {
                session.close();
            }
            return true;
        }
        return false;
    }

    /*static synchronized void incrementFailure(User u) {
        if (u != null) {
            session = HibernateUtil.getSessionFactory().openSession();
            Transaction transaction = session.beginTransaction();
            u.setFailedReservations((byte) (u.getFailedReservations()+1));
            if(u.getFailedReservations()==3){
                u.setStatus("blocked");
            }
            session.update(u);
            transaction.commit();
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }*/
    public static User findByGEmail(String g_email) {
        session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        User usr = null;
        Query query = session.createQuery("from User where GId=:g_email");
        try {
            query.setParameter("g_email", g_email);
            usr = (User) query.list().get(0);
            transaction.commit();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            transaction.rollback();
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return usr;
    }

    public static User registerG(String email, String firstName, String lastName) {
        User usr;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Transaction transaction = session.beginTransaction();
            usr = new User(firstName, lastName, email, null, null, email, "regular", null, null, email, false, (byte) 0, null, null, null);
            session.persist(usr);
            transaction.commit();
        } catch (ConstraintViolationException e) {
            usr = null;
        }
        return usr;
    }

}
