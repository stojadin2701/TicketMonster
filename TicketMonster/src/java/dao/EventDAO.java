/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import static dao.ReservationDAO.session;
import static dao.UserDAO.session;
import entity.Event;
import entity.Image;
import entity.Performer;
import entity.User;
import entity.Video;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import util.HibernateUtil;
import util.SearchContainer;

/**
 *
 * @author Nemanja
 */
public class EventDAO {

    static Session session = null;

    public static void store(Event ev) {
        session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        session.persist(ev);
        transaction.commit();
        if (session != null && session.isOpen()) {
            session.close();
        }
    }

    public static List<Event> getAll() {
        List<Event> evList = null;
        session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        Query query = session.createQuery("from Event where cancelled=0 order by start_date desc");
        try {
            evList = query.list();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return evList;
    }

    public static List<Event> getByDate(Date selectedDate) {
        List<Event> evList = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String strDate = sdf.format(selectedDate);
        session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        Query query = session.createQuery("from Event where :selected_date between start_date and end_date order by start_date desc");
        try {
            query.setParameter("selected_date", strDate);
            evList = query.list();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return evList;
    }

    public static List<Event> search(SearchContainer cont) {
        List<Event> evList = null;
        List<Event> retList = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String searchNameStr = null;
        String searchStartDate = null;
        String searchEndDate = null;
        String searchVenueStr = null;
        String searchPerfStr = null;
        String strToday = sdf.format(new Date());
        if (cont.getName() != null && !"".equals(cont.getName())) {
            searchNameStr = " and (event_name like :ev_name)";
        }
        if (cont.getStartDate() != null) {
            searchStartDate = " and (start_date >= :s_date)";
        }
        if (cont.getEndDate() != null) {
            searchEndDate = " and (end_date <= :e_date)";
        }
        if (cont.getPlace() != null && !"".equals(cont.getPlace())) {
            searchVenueStr = " and (event_place like :ev_place)";
        }

        session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        String queryStr = "from Event where cancelled=0 and end_date > :today "
                + (searchNameStr != null ? searchNameStr : "")
                + (searchStartDate != null ? searchStartDate : "")
                + (searchEndDate != null ? searchEndDate : "")
                + (searchVenueStr != null ? searchVenueStr : "")
                + " order by start_date desc";
        Query query = session.createQuery(queryStr);
        try {
            query.setParameter("today", strToday);
            if (searchNameStr != null) {
                query.setParameter("ev_name", "%" + cont.getName() + "%");
            }
            if (searchVenueStr != null) {
                query.setParameter("ev_place", "%" + cont.getPlace() + "%");
            }
            if (cont.getStartDate() != null) {
                query.setParameter("s_date", sdf.format(cont.getStartDate()));
            }
            if (cont.getEndDate() != null) {
                query.setParameter("e_date", sdf.format(cont.getEndDate()));
            }
            evList = query.list();
            if (cont.getPerformerName() != null && !("".equals(cont.getPerformerName()))) {
                retList = new ArrayList<>();
                for (Event ev : evList) {
                    for (Performer per : new ArrayList<Performer>(ev.getPerformers())) {
                        if (per.getPerformerName().toLowerCase().contains(cont.getPerformerName().toLowerCase())) {
                            retList.add(ev);
                            break;
                        }
                    }
                }
            } else {
                retList = evList;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return retList;
    }

    public static List<Event> topRated() {
        List<Event> evList = null;
        session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        Query query = session.createQuery("from Event where cancelled=0 order by rating desc");
        try {
            query.setFirstResult(0);
            query.setMaxResults(5);
            evList = query.list();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return evList;
    }

    public static List<Event> ongoingUpcoming() {
        List<Event> evList = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String strDate = sdf.format(new Date());
        session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        Query query = session.createQuery("from Event where cancelled=0 and ((:today between start_date and end_date) or (start_date > :today)) order by start_date asc");
        try {
            query.setFirstResult(0);
            query.setMaxResults(5);
            query.setParameter("today", strDate);
            evList = query.list();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return evList;
    }

    public static List<Event> mostViewed() {
        List<Event> evList = null;
        session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        Query query = session.createQuery("from Event where cancelled=0 order by view_count desc");
        try {
            query.setFirstResult(0);
            query.setMaxResults(5);
            evList = query.list();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return evList;
    }

    public static List<Event> mostVisited() {
        List<Event> evList = null;
        session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        Query query = session.createQuery("from Event where cancelled=0 order by sell_count desc");
        try {
            query.setFirstResult(0);
            query.setMaxResults(5);
            evList = query.list();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return evList;
    }

    public static List<Image> getApprovedImages(Event ev) {
        List<Image> ilist = null;
        session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        Query query = session.createQuery("from Image where event_id=:ev_id and approved=1");
        try {
            query.setParameter("ev_id", ev.getEventId());
            ilist = query.list();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return ilist;
    }

    public static List<Video> getApprovedVideos(Event ev) {
        List<Video> vlist = null;
        session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        Query query = session.createQuery("from Video where event_id=:ev_id and approved=1");
        try {
            query.setParameter("ev_id", ev.getEventId());
            vlist = query.list();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return vlist;
    }

    public static synchronized void incrementViewCount(Event ev) {
        if (ev != null) {
            session = HibernateUtil.getSessionFactory().openSession();
            Transaction transaction = session.beginTransaction();
            ev.setViewCount(ev.getViewCount() + 1);
            session.update(ev);
            transaction.commit();
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    public static List<Event> getSellable() {
        List<Event> evList;
        List<Event> retList = new ArrayList<>();
        session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        Query query = session.createQuery("from Event e where e.cancelled=0 and (select sum(tr.available) from TicketsRemaining tr where tr.id.eventId=e.eventId)>0)");
        try {
            evList = query.list();
            for (Event ev : evList) {
                if(ev.getEndDate().after(new Date())) retList.add(ev);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return retList;
    }

    public static void cancel(Event ev) {
        if (ev != null) {
            session = HibernateUtil.getSessionFactory().openSession();
            Transaction transaction = session.beginTransaction();
            ev.setCancelled(true);
            session.update(ev);
            transaction.commit();
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    public static synchronized void increaseSellCount(Event ev, boolean one) {
        if (ev != null) {
            session = HibernateUtil.getSessionFactory().openSession();
            Transaction transaction = session.beginTransaction();
            try {
                if (one) {
                    ev.setSellCount(ev.getSellCount() + 1);
                } else {
                    Query query = session.createQuery("select count(*) from TicketsRemaining where event_id=:ev_id");
                    query.setParameter("ev_id", ev.getEventId());
                    Long totalDays = (Long) query.uniqueResult();
                    ev.setSellCount((int) (ev.getSellCount() + totalDays));
                }
                session.update(ev);
                transaction.commit();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                transaction.rollback();
            } finally {
                if (session != null && session.isOpen()) {
                    session.close();
                }
            }
        }
    }

    public static void updateRating(String eventName) {
        if (eventName != null) {
            session = HibernateUtil.getSessionFactory().openSession();
            Transaction transaction = session.beginTransaction();
            try {
                Query query = session.createQuery("select avg(ur.rating) from UserReview ur where ur.eventName=:ev_name and ur.rating is not null");
                query.setParameter("ev_name", eventName);
                Double rating = (Double) query.list().get(0);
                query = session.createQuery("from Event where eventName=:ev_name");
                query.setParameter("ev_name", eventName);
                List<Event> evList = query.list();
                for (Event ev : evList) {
                    ev.setRating(rating != null ? rating.floatValue() : 0);
                    session.update(ev);
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
        }
    }

    public static void update(Event ev) {
        session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        session.update(ev);
        transaction.commit();
        if (session != null && session.isOpen()) {
            session.close();
        }
    }

}
