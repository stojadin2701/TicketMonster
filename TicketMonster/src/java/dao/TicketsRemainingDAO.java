/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import static dao.EventDAO.session;
import static dao.ImageDAO.session;
import entity.Event;
import entity.Reservation;
import entity.TicketsRemaining;
import entity.TicketsRemainingId;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import util.HibernateUtil;

/**
 *
 * @author Nemanja
 */
public class TicketsRemainingDAO {

    static Session session = null;

    public static void store(TicketsRemaining tr) {
        session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        session.persist(tr);
        transaction.commit();
        if (session != null && session.isOpen()) {
                session.close();
            }
    }

    public static void storeMultiple(List<TicketsRemaining> trlist) {
        for (TicketsRemaining tr : trlist) {
            store(tr);
        }
    }

    public static void addTicketsRemainingFromEvent(Event ev) {
        TicketsRemaining tr = null;
        TicketsRemainingId trid = null;
        LocalDate ldStart = ev.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate ldEnd = ev.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        int day = 1;

        while (!ldStart.isAfter(ldEnd)) {
            trid = new TicketsRemainingId(ev.getEventId(), day++);
            tr = new TicketsRemaining(trid, ev, ev.getMaxTicketsAvailablePerDay());
            ev.getTicketsRemainings().add(tr);
            ldStart = ldStart.plusDays(1);
        }

        storeMultiple(new ArrayList(ev.getTicketsRemainings()));
    }

    public synchronized static boolean canBuyAll(Event ev) {
        List<TicketsRemaining> trList = null;
        session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        Query query = session.createQuery("from TicketsRemaining where event_id=:ev_id");
        try {
            query.setParameter("ev_id", ev.getEventId());
            trList = query.list();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }

        for (TicketsRemaining tr : trList) {
            if (tr.getAvailable() <= 0) {
                return false;
            }
        }
        return true;
    }

    public synchronized static void reduceAll(Event ev) {
        List<TicketsRemaining> trList = null;
        session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        Query query = session.createQuery("from TicketsRemaining where event_id=:ev_id");
        try {
            query.setParameter("ev_id", ev.getEventId());
            trList = query.list();
            for (TicketsRemaining tr : trList) {
                tr.setAvailable(tr.getAvailable() - 1);
                session.update(tr);
            }
            transaction.commit();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    public synchronized static void reduceOne(Event ev, int day) {
        TicketsRemaining tr;
        session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        Query query = session.createQuery("from TicketsRemaining where event_id=:ev_id and event_day=:ev_day");
        try {
            query.setParameter("ev_id", ev.getEventId());
            query.setParameter("ev_day", day);
            tr = (TicketsRemaining) query.list().get(0);
            tr.setAvailable(tr.getAvailable() - 1);
            session.update(tr);
            transaction.commit();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    public synchronized static List<Integer> getAvailableDays(Event ev) {
        List<TicketsRemaining> trList = null;
        List<Integer> availableList = new ArrayList<>();
        session = HibernateUtil.getSessionFactory().openSession();
        Query query = session.createQuery("from TicketsRemaining where event_id=:ev_id");
        try {
            query.setParameter("ev_id", ev.getEventId());
            trList = query.list();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }

        for (TicketsRemaining tr : trList) {
            if (tr.getAvailable() > 0) {
                availableList.add(tr.getId().getEventDay());
            }
        }

        return availableList;
    }

    /*static void returnOneDayTicket(Reservation pr) {
        TicketsRemaining tr;
        Query query = session.createQuery("from TicketsRemaining where event_id=:ev_id and event_day=:ev_day");
        Transaction transaction = session.beginTransaction();
        try {
            query.setParameter("ev_id", pr.getEvent().getEventId());
            tr = (TicketsRemaining) query.list().get(0);
            if (tr != null) {
                tr.setAvailable(tr.getAvailable() + 1);
                session.update(tr);
                transaction.commit();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }*/

    /*static void returnAllDaysTicket(Reservation pr) {
        List<TicketsRemaining> trList = null;
        session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        Query query = session.createQuery("from TicketsRemaining where event_id=:ev_id");
        try {
            query.setParameter("ev_id", pr.getEvent().getEventId());
            trList = query.list();
            for (TicketsRemaining tr : trList) {
                tr.setAvailable(tr.getAvailable() + 1);
                session.update(tr);
            }
            transaction.commit();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }*/

    public static void updateTicketsRemainingFromEvent(Event ev, int ticketDiff, int dayDiff) {
        session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        for (Object tro : ev.getTicketsRemainings()) {
            TicketsRemaining tr = (TicketsRemaining)tro;
            tr.setAvailable(tr.getAvailable()+ticketDiff);
            session.update(tr);
        }
        transaction.commit();
        if (session != null && session.isOpen()) {
            session.close();
        }
    }
}
