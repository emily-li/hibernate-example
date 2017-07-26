package com.liemily.hibernateexample.dataaccess;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.util.List;

/**
 * Created by Emily Li on 25/07/2017.
 */
@Component
public class HibernateDAL {
    private static final Logger logger = LogManager.getLogger(HibernateDAL.class);
    private SessionFactory sessionFactory;

    @Autowired
    public HibernateDAL(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @PostConstruct
    public void init() {
        sessionFactory.openSession();
    }

    @Transactional
    public List query(String query) throws InterruptedException {
        try (Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();
            List result = session.createQuery(query).list();
            session.getTransaction().commit();
            return result;
        } catch (HibernateException e) {
            final long WAIT_MS = 250;
            if (e.getMessage().contains("no connection is currently available")) {
                logger.warn("No connection available in the connection pool. Sleeping " + WAIT_MS + "ms", e);
                Thread.sleep(WAIT_MS);
                return query(query);
            }
            throw e;
        }
    }

    @Transactional
    public void execute(String query) {
        try (Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();
            session.createQuery(query).executeUpdate();
            session.getTransaction().commit();
        }
    }

    @Transactional
    public void save(Object object) {
        try (Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();
            session.save(object);
            session.getTransaction().commit();
        }
    }

    @Transactional
    public void delete(String entity, String id) {
        try (Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();
            Object objectToDelete = session.load(entity, id);
            session.delete(objectToDelete);
            session.getTransaction().commit();
        }
    }
}
