/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import static dao.ImageDAO.session;
import static dao.ReservationDAO.session;
import entity.Message;
import entity.Reservation;
import entity.User;
import java.util.List;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import util.HibernateUtil;

/**
 *
 * @author Nemanja
 */
public class MessageDAO {

    static Session session = null;

    public static final byte EVENT_CANCELLED = 0;
    public static final byte PERFORMER_CHANGED = 1;
    public static final byte TIMETABLE_CHANGED = 2;

    public static void addFromReservation(Reservation res, byte type) {
        if (!res.isCancelledEvent()) {
            Message msg = null;
            if (type == EVENT_CANCELLED) {
                if ("pending".equals(res.getStatus())) {
                    msg = new Message(res.getEvent(), res.getUser(), "reserved");
                } else if ("sold".equals(res.getStatus())) {
                    msg = new Message(res.getEvent(), res.getUser(), "sold");
                }
            } else {
                if ("pending".equals(res.getStatus()) || "sold".equals(res.getStatus())) {
                    if (type == PERFORMER_CHANGED) {
                        msg = new Message(res.getEvent(), res.getUser(), "performer");
                    } else if (type == TIMETABLE_CHANGED) {
                        msg = new Message(res.getEvent(), res.getUser(), "timetable");
                    }
                }
            }
            if (msg != null) {
                session = HibernateUtil.getSessionFactory().openSession();
                Transaction transaction = session.beginTransaction();
                session.persist(msg);
                transaction.commit();
                if (session != null && session.isOpen()) {
                    session.close();
                }
            }
        }
    }

    public static List<Message> getForUserAndDelete(User usr) {
        session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();

        List<Message> messagesForUser = null;

        Query query = session.createQuery("from Message m join fetch m.event join fetch m.user where m.user.userId=:usr_id ");
        try {
            query.setParameter("usr_id", usr.getUserId());
            messagesForUser = query.list();
            for (Message msg : messagesForUser) {
                session.delete(msg);
            }
            transaction.commit();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            transaction.rollback();
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return messagesForUser;
    }

}
