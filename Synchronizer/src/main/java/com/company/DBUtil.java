package com.company;

import java.sql.Connection;
import java.sql.Statement;
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
   * Forms SQL Batch and executes it in one transaction. Rolls back changes if any error occurred
   * during execution of SQL Batch
   *
   * @param sqlStatements
   *          list of SQL DML statements (INSERT, UPDATE, DELETE) to execute in one transaction
   */
  static void writeToDB(final List<String> sqlStatements) {
    try {
      connectToDB();
      final Statement stmt = null;
      final StringBuilder sqlBatch = new StringBuilder();
      sqlBatch.append("BEGIN TRY\n");
      sqlBatch.append("BEGIN TRANSACTION \n");
      for (String sqlStmt : sqlStatements) {
        sqlBatch.append(sqlStmt).append("\n");
      }
      sqlBatch.append("COMMIT\n");
      sqlBatch.append("END TRY\n");
      sqlBatch.append("BEGIN CATCH\n");
      sqlBatch.append("    IF @@TRANCOUNT > 0\n");
      sqlBatch.append("        ROLLBACK\n");
      sqlBatch.append("END CATCH");
      final String sqlBatchStr = sqlBatch.toString();
      stmt.addBatch(sqlBatchStr);
      if (logger.isDebugEnabled()) {
        logger.debug("SQL Batch to execute:\n" + sqlBatchStr);
      }
      stmt.executeLargeBatch();
      stmt.close();
    } catch (Exception e) {
      logger.fatal(String.format("Fatal error during writing data to DB: %s", e.getMessage()));
      throw new RuntimeException("Fatal error during writing data to DB!");
    }
  }

  /**
   * Connects to MS SQL DB using properties from {@link Properties} file
   *
   * @return {@link Connection} object
   */
  public static void connectToDB() {
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


  // /* Method to CREATE an employee in the database */
  // public Integer addEmployee(String fname, String lname, int salary){
  // Session session = sessionFactory.openSession();
  // Transaction tx = null;
  // Integer employeeID = null;
  // try{
  // tx = session.beginTransaction();
  // Employee employee = new Employee(fname, lname, salary);
  // employeeID = (Integer) session.save(employee);
  // tx.commit();
  // }catch (HibernateException e) {
  // if (tx!=null) tx.rollback();
  // e.printStackTrace();
  // }finally {
  // session.close();
  // }
  // return employeeID;
  // }

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
  // /* Method to UPDATE salary for an employee */
  // public void updateEmployee(Integer EmployeeID, int salary ){
  // Session session = factory.openSession();
  // Transaction tx = null;
  // try{
  // tx = session.beginTransaction();
  // Employee employee =
  // (Employee)session.get(Employee.class, EmployeeID);
  // employee.setSalary( salary );
  // session.update(employee);
  // tx.commit();
  // }catch (HibernateException e) {
  // if (tx!=null) tx.rollback();
  // e.printStackTrace();
  // }finally {
  // session.close();
  // }
  // }
  // /* Method to DELETE an employee from the records */
  // public void deleteEmployee(Integer EmployeeID){
  // Session session = factory.openSession();
  // Transaction tx = null;
  // try{
  // tx = session.beginTransaction();
  // Employee employee =
  // (Employee)session.get(Employee.class, EmployeeID);
  // session.delete(employee);
  // tx.commit();
  // }catch (HibernateException e) {
  // if (tx!=null) tx.rollback();
  // e.printStackTrace();
  // }finally {
  // session.close();
  // }
  // }
}
