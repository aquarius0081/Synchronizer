package com.company;

import java.sql.Connection;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

/**
 * Utility class for working with DB table
 */
class DBUtil {

  private static SessionFactory sessionFactory;

  /**
   * Instance of {@link Logger} class for {@link DBUtil}
   */
  private final static Logger logger = Logger.getLogger(DBUtil.class);

  /**
   * Connects to MS SQL DB using properties from {@link Properties} file
   *
   * @return {@link Connection} object
   */
  public static void connectToDB() {
    if (sessionFactory == null) {
      StandardServiceRegistry registry = null;
      try {
        // A SessionFactory is set up once for an application!
        registry = new StandardServiceRegistryBuilder().configure() // configures settings from
            // hibernate.cfg.xml
            .build();
        sessionFactory = new MetadataSources(registry).buildMetadata().buildSessionFactory();
      } catch (Exception e) {
        // The registry would be destroyed by the SessionFactory, but we had trouble building the
        // SessionFactory
        // so destroy it manually.
        StandardServiceRegistryBuilder.destroy(registry);
      }
    }
  }

  /**
   * Method to CREATE a job in the database
   *
   * @param depcode
   * @param depjob
   * @param description
   * @return
   */
  public static Integer addJob(String depcode, String depjob, String description) {
    Session session = sessionFactory.openSession();
    Transaction tx = null;
    Integer jobId = null;
    try {
      tx = session.beginTransaction();
      Job job = new Job(depcode, depjob, description);
      jobId = (Integer) session.save(job);
      tx.commit();
    } catch (HibernateException e) {
      if (tx != null)
        tx.rollback();
      e.printStackTrace();
    } finally {
      session.close();
    }
    return jobId;
  }

  /**
   * Method to READ all the jobs
   */
  public static List getJobs() {
    Transaction tx = null;
    List jobs = null;
    try (Session session = sessionFactory.openSession()) {
      tx = session.beginTransaction();
      jobs = session.createQuery("FROM Job").list();
      tx.commit();
    } catch (HibernateException e) {
      if (tx != null)
        tx.rollback();
      e.printStackTrace();
    }
    return jobs;
  }



  /**
   * Method to UPDATE description for a job
   *
   * @param id
   * @param description
   */
  public static void updateJob(Long id, String description) {
    Session session = sessionFactory.openSession();
    Transaction tx = null;
    try {
      tx = session.beginTransaction();
      Job job = session.get(Job.class, id);
      job.setDescription(description);
      session.update(job);
      tx.commit();
    } catch (HibernateException e) {
      if (tx != null)
        tx.rollback();
      e.printStackTrace();
    } finally {
      session.close();
    }
  }

  /**
   * Method to DELETE a job from the records
   *
   * @param id
   */
  public static void deleteJob(Long id) {
    Session session = sessionFactory.openSession();
    Transaction tx = null;
    try {
      tx = session.beginTransaction();
      Job job = session.get(Job.class, id);
      session.delete(job);
      tx.commit();
    } catch (HibernateException e) {
      if (tx != null)
        tx.rollback();
      e.printStackTrace();
    } finally {
      session.close();
    }
  }
}
