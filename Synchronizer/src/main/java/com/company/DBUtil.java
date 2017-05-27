package com.company;

import jdk.nashorn.internal.scripts.JO;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.*;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

/**
 * Utility class for working with DB table
 */
class DBUtil {

  /**
   * Instance of {@link Logger} class for {@link DBUtil}
   */
  private final static Logger logger = Logger.getLogger(DBUtil.class);

  private static SessionFactory factory;

  /**
   * Reads data from DB table and puts it into intermediate {@link HashSet} of {@link Job} objects
   *
   * @return {@link HashSet} of {@link Job} objects
   */
  static HashSet<Job> readFromDB() {
    final HashSet<Job> jobs = new HashSet<>();
    try (final Connection con = getConnectionToDB()) {
      final Statement stmt = con.createStatement();
      final ResultSet rs = stmt.executeQuery("SELECT * FROM Job");
      while (rs.next()) {
        jobs.add(new Job() {{
          setDepcode(rs.getString("DepCode"));
          setDepjob(rs.getString("DepJob"));
          setDescription(rs.getString("Description"));
        }});
      }
      rs.close();
      stmt.close();
    } catch (Exception e) {
      logger.fatal(String.format("Fatal error during reading data from DB: %s", e.getMessage()));
      throw new RuntimeException("Fatal error during reading data from DB!");
    }
    return jobs;
  }

  /**
   * Forms SQL Batch and executes it in one transaction.
   * Rolls back changes if any error occurred during execution of SQL Batch
   *
   * @param sqlStatements list of SQL DML statements (INSERT, UPDATE, DELETE) to execute in one transaction
   */
  static void writeToDB(final List<String> sqlStatements) {
    try (final Connection con = getConnectionToDB()) {
      final Statement stmt = con.createStatement();
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
  private static Connection getConnectionToDB() {
    final Connection connection;
    try {
      final Properties properties = new Properties();
      properties.load(new FileInputStream("synchronizer.properties"));
//      try{
//        factory = new Configuration().configure().buildSessionFactory();
//      }catch (Throwable ex) {
//        System.err.println("Failed to create sessionFactory object." + ex);
//        throw new ExceptionInInitializerError(ex);
//      }
//      Session session = factory.openSession();
//      Transaction tx = null;
//      try {
//        tx = session.beginTransaction();
//        // do some work
//        tx.commit();
//      } catch (Exception e) {
//        if (tx != null) tx.rollback();
//        e.printStackTrace();
//      } finally {
//        session.close();
//      }

      DriverManager.registerDriver(new com.microsoft.sqlserver.jdbc.SQLServerDriver());
      connection = DriverManager.getConnection(properties.getProperty("db.connectionString"),
          properties.getProperty("db.user"),
          properties.getProperty("db.password"));
    } catch (FileNotFoundException e) {
      final String fatalErrorMessage = "The synchronizer.properties file is not found!";
      logger.fatal(fatalErrorMessage);
      throw new RuntimeException(fatalErrorMessage);
    } catch (IOException e) {
      logger.fatal(String.format("Error occurred when loading from the synchronizer.properties file: %s", e.getMessage()));
      throw new RuntimeException("Error occurred when reading from the synchronizer.properties file!");
    } catch (SQLException e) {
      logger.fatal(String.format("Cannot connect to DB: %s", e.getMessage()));
      throw new RuntimeException("Cannot connect to DB!");
    }
    return connection;
  }

  protected static void setUp() throws Exception {
    // A SessionFactory is set up once for an application!
    final StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
        .configure() // configures settings from hibernate.cfg.xml
        .build();
    try {
      factory = new MetadataSources( registry ).buildMetadata().buildSessionFactory();
    }
    catch (Exception e) {
      // The registry would be destroyed by the SessionFactory, but we had trouble building the SessionFactory
      // so destroy it manually.
      StandardServiceRegistryBuilder.destroy( registry );
    }
  }
  /* Method to CREATE an employee in the database */
  public static Integer addJob(String depcode, String depjob, String description) {
    try {
      setUp();
    } catch (Exception e) {
      e.printStackTrace();
    }
    Session session = factory.openSession();
    Transaction tx = null;
    Integer jobID = null;
    try {
      tx = session.beginTransaction();
      Job job = new Job(depcode, depjob, description);
      jobID = (Integer) session.save(job);
      tx.commit();
    } catch (HibernateException e) {
      if (tx != null) tx.rollback();
      e.printStackTrace();
    } finally {
      session.close();
    }
    return jobID;
  }

//  /* Method to  READ all the employees */
//  public void listEmployees() {
//    Session session = factory.openSession();
//    Transaction tx = null;
//    try {
//      tx = session.beginTransaction();
//      List employees = session.createQuery("FROM Employee").list();
//      for (Iterator iterator =
//           employees.iterator(); iterator.hasNext(); ) {
//        Employee employee = (Employee) iterator.next();
//        System.out.print("First Name: " + employee.getFirstName());
//        System.out.print("  Last Name: " + employee.getLastName());
//        System.out.println("  Salary: " + employee.getSalary());
//      }
//      tx.commit();
//    } catch (HibernateException e) {
//      if (tx != null) tx.rollback();
//      e.printStackTrace();
//    } finally {
//      session.close();
//    }
//  }
//
//  /* Method to UPDATE salary for an employee */
//  public void updateEmployee(Integer EmployeeID, int salary) {
//    Session session = factory.openSession();
//    Transaction tx = null;
//    try {
//      tx = session.beginTransaction();
//      Employee employee =
//          (Employee) session.get(Employee.class, EmployeeID);
//      employee.setSalary(salary);
//      session.update(employee);
//      tx.commit();
//    } catch (HibernateException e) {
//      if (tx != null) tx.rollback();
//      e.printStackTrace();
//    } finally {
//      session.close();
//    }
//  }
//
//  /* Method to DELETE an employee from the records */
//  public void deleteEmployee(Integer EmployeeID) {
//    Session session = factory.openSession();
//    Transaction tx = null;
//    try {
//      tx = session.beginTransaction();
//      Employee employee =
//          (Employee) session.get(Employee.class, EmployeeID);
//      session.delete(employee);
//      tx.commit();
//    } catch (HibernateException e) {
//      if (tx != null) tx.rollback();
//      e.printStackTrace();
//    } finally {
//      session.close();
//    }
//  }
}