/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import static dao.TicketsRemainingDAO.session;
import static dao.UserDAO.session;
import entity.Event;
import entity.Reservation;
import entity.TicketsRemaining;
import entity.User;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import util.HibernateUtil;

/**
 *
 * @author Nemanja
 */
public class ReservationDAO {

    static Session session = null;

    public static void reserve(User u, Event ev, boolean whole, Integer daySelected) {
        Reservation res = new Reservation(ev, u, "pending", new Date(), whole ? "whole" : "one_day", false, daySelected);
        session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        session.persist(res);
        transaction.commit();
        if (session != null && session.isOpen()) {
            session.close();
        }
    }

    public static void changeStatus(Reservation res, String status) {
        if ("pending".equals(status) || "sold".equals(status) || "failed".equals(status) || "cancelled_by_user".equals(status)) {
            session = HibernateUtil.getSessionFactory().openSession();
            Transaction transaction = session.beginTransaction();
            res.setStatus(status);
            if ("sold".equals(status)) {
                if ("one_day".equals(res.getType())) {
                    res.getEvent().setSellCount(res.getEvent().getSellCount() + 1);
                } else {
                    Query query = session.createQuery("select count(*) from TicketsRemaining where event_id=:ev_id");
                    query.setParameter("ev_id", res.getEvent().getEventId());
                    Long totalDays = (Long) query.uniqueResult();
                    res.getEvent().setSellCount((int) (res.getEvent().getSellCount() + totalDays));
                }
                session.update(res.getEvent());
            }
            if ("cancelled_by_user".equals(status)) {
                if ("one_day".equals(res.getType())) {
                    TicketsRemaining tr;
                    Query query = session.createQuery("from TicketsRemaining where event_id=:ev_id and event_day=:ev_day");
                    query.setParameter("ev_id", res.getEvent().getEventId());
                    query.setParameter("ev_day", res.getDaySelected());
                    tr = (TicketsRemaining) query.list().get(0);
                    if (tr != null) {
                        tr.setAvailable(tr.getAvailable() + 1);
                        session.update(tr);
                    }
                } else {
                    List<TicketsRemaining> trList;
                    Query query = session.createQuery("from TicketsRemaining where event_id=:ev_id");
                    query.setParameter("ev_id", res.getEvent().getEventId());
                    trList = query.list();
                    for (TicketsRemaining tr : trList) {
                        tr.setAvailable(tr.getAvailable() + 1);
                        session.update(tr);
                    }
                }
            }
            session.update(res);
            transaction.commit();
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    public static List<Reservation> getPending() {
        session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();

        List<Reservation> pendingList = null;

        Query query = session.createQuery("from Reservation r join fetch r.event join fetch r.user where r.status = 'pending' and r.cancelledEvent=false order by r.reservationDatetime asc");
        try {
            pendingList = query.list();

            LocalDate today = (new Date()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            ListIterator<Reservation> iter = pendingList.listIterator();
            while (iter.hasNext()) {
                Reservation pr = iter.next();
                LocalDate ldResDate = pr.getReservationDatetime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                if ((ldResDate.plusDays(2)).isBefore(today)) {
                    pr.setStatus("failed");
                    User u = pr.getUser();
                    if (u != null) {
                        u.setFailedReservations((byte) (u.getFailedReservations() + 1));
                        if (u.getFailedReservations() == 3) {
                            u.setStatus("blocked");
                        }
                        session.update(u);
                    }
                    session.update(pr);
                    if ("one_day".equals(pr.getType())) {
                        TicketsRemaining tr;
                        query = session.createQuery("from TicketsRemaining where event_id=:ev_id and event_day=:ev_day");
                        query.setParameter("ev_id", pr.getEvent().getEventId());
                        query.setParameter("ev_day", pr.getDaySelected());
                        tr = (TicketsRemaining) query.list().get(0);
                        if (tr != null) {
                            tr.setAvailable(tr.getAvailable() + 1);
                            session.update(tr);
                        }
                    } else {
                        List<TicketsRemaining> trList;
                        query = session.createQuery("from TicketsRemaining where event_id=:ev_id");
                        query.setParameter("ev_id", pr.getEvent().getEventId());
                        trList = query.list();
                        for (TicketsRemaining tr : trList) {
                            tr.setAvailable(tr.getAvailable() + 1);
                            session.update(tr);
                            //TicketsRemainingDAO.returnAllDaysTicket(pr);
                        }
                    }
                    iter.remove();
                }
            }
            transaction.commit();
            //refresh(pendingList);
            //transaction.commit();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            transaction.rollback();
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return pendingList;
    }

    public static List<Reservation> getReservationsForUser(User usr) {
        session = HibernateUtil.getSessionFactory().openSession();
        //Transaction transaction = session.beginTransaction();

        List<Reservation> pendingList = null;

        Query query = session.createQuery("from Reservation r join fetch r.event join fetch r.user where r.user.userId=:usr_id order by r.reservationDatetime desc");
        try {
            query.setParameter("usr_id", usr.getUserId());
            pendingList = query.list();
            //transaction.commit();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            //transaction.rollback();
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return pendingList;
    }

    /*public static void refresh(List<Reservation> pendingReservations) {
        session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        LocalDate today = (new Date()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        try {
            for (Reservation pr : pendingReservations) {
                LocalDate ldResDate = pr.getReservationDatetime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                if ((ldResDate.plusDays(2)).isBefore(today)) {
                    pendingReservations.remove(pr);
                    pr.setStatus("failed");
                    User u = pr.getUser();
                    if (u != null) {
                        u.setFailedReservations((byte) (u.getFailedReservations() + 1));
                        if (u.getFailedReservations() == 3) {
                            u.setStatus("blocked");
                        }
                        session.update(u);
                        transaction.commit();
                    }
                    session.update(pr);
                    if ("one_day".equals(pr.getType())) {
                        TicketsRemainingDAO.returnOneDayTicket(pr);
                    } else {
                        TicketsRemainingDAO.returnAllDaysTicket(pr);
                    }
                }
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
    }*/
    public static void setEventCancelled(Reservation res) {
        if (res != null) {
            session = HibernateUtil.getSessionFactory().openSession();
            Transaction transaction = session.beginTransaction();
            res.setCancelledEvent(true);
            session.update(res);
            transaction.commit();
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    public static short numOfReservationsUserEvent(User usr, Event ev) {
        short retVal = Short.MAX_VALUE;
        if (usr != null && ev != null) {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("select count(*) from Reservation r where r.event.eventId=:ev_id and r.user.userId=:usr_id and r.status!='cancelled_by_user'");
            query.setParameter("ev_id", ev.getEventId());
            query.setParameter("usr_id", usr.getUserId());
            retVal = ((Long) query.uniqueResult()).shortValue();
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return retVal;
    }

    public static boolean boughtTicket(Event ev, User usr) {
        session = HibernateUtil.getSessionFactory().openSession();
        List<Reservation> rList=null;
        Query query = session.createQuery("from Reservation r join fetch r.event join fetch r.user where r.status='sold' and r.event.eventId=:ev_id and r.user.userId=:usr_id");
        try {
            query.setParameter("ev_id", ev.getEventId());
            query.setParameter("usr_id", usr.getUserId());
            rList = query.list();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        if (rList == null || rList.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

}
