package com.company;

import org.apache.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


/**
 * Main class of application. Using this application you can export data from configured DB table to
 * XML file and synchronize data from specified XML file to DB table.
 */
@SpringBootApplication
public class Main extends SpringBootServletInitializer {

  /**
   * Instance of {@link Logger} class for {@link Main}
   */
  private static final Logger logger = Logger.getLogger(Main.class);

  /**
   * Instance {@link Marshaller} used for marshalling of intermediate {@link Jobs} object into XML
   * file
   */
  private static final ThreadLocal<Marshaller> marshaller = ThreadLocal.withInitial(() -> {
    try {
      final JAXBContext jaxbContext = JAXBContext.newInstance(Jobs.class);
      final Marshaller marshaller = jaxbContext.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
      return marshaller;
    } catch (JAXBException e) {
      final String fatalErrorMessage = "Fatal error during creation of marshaller";
      logger.fatal(fatalErrorMessage);
      throw new RuntimeException(fatalErrorMessage);
    }
  });

  /**
   * Performs 2 operations dependent on arguments either export or synchronization
   *
   * @param args arguments usage: export|sync fileName.xml
   */
  public static void main(String[] args) {
    SpringApplication.run(Main.class, args);
  }

  /**
   * Exports data from DB table to specified XML file. First reads data from DB table, puts it to
   * intermediate {@link Jobs} object then marshals that object to XML file.
   *
   * @param path path to XML file with exported data from DB table
   */
  public static void exportToXml(final String path) {
    if (logger.isDebugEnabled()) {
      logger.debug("Start read data from DB.");
    }
    final Jobs jobs = new Jobs();
    jobs.setJobs(DBUtil.getJobs());
    if (logger.isDebugEnabled()) {
      logger.debug("Reading data from DB completed successfully.");
    }
    final File outXml = new File(path);

    if (logger.isDebugEnabled()) {
      logger.debug("Start marshalling DB data to XML file.");
    }
    try {
      marshaller.get().marshal(jobs, outXml);
    } catch (JAXBException e) {
      logger.fatal(
          String.format("Fatal error during marshalling DB data to XML file: %s", e.getMessage()));
      throw new RuntimeException("Fatal error during marshalling DB data to XML file");
    }
    if (logger.isDebugEnabled()) {
      logger.debug("Marshalling DB data to XML file completed successfully.");
    }
  }

  /**
   * Synchronizes data from specified XML file to DB table. First reads data from XML file, puts it
   * into intermediate {@link HashSet} of {@link Job} objects. Then reads data from DB table and
   * puts it to another {@link HashSet} of {@link Job} objects. Using this info forms SQL Batch to
   * execute in one transaction and executes it. If any error during this process, no data in DB
   * table will be changed.
   *
   * @param path path to XML file with data for synchronization
   */
  public static void syncFromXml(final String path) {
    if (logger.isDebugEnabled()) {
      logger.debug("Start read data from XML file.");
    }
    final List<Job> jobsFromXml = XMLUtil.readFromXml(path);
    if (logger.isDebugEnabled()) {
      logger.debug("Reading data from XML file completed successfully.");
      logger.debug("Start read data from DB.");
    }
    final List<Job> jobsFromDB = DBUtil.getJobs();
    if (logger.isDebugEnabled()) {
      logger.debug("Reading data from DB completed successfully.");
      logger.debug("Start forming SQL statements to sync DB data from XML file.");
    }
    final List<String> sqlStatements = new ArrayList<>();

    // UPDATE SQL statements
    jobsFromXml.forEach((x) -> jobsFromDB.forEach((d) -> {
      if (d.getDepcode().equals(x.getDepcode()) && d.getDepjob().equals(x.getDepjob())
          && !d.getDescription().equals(x.getDescription())) {
        DBUtil.updateJob(d.getId(), x.getDescription());
      }
    }));
    if (logger.isDebugEnabled()) {
      logger.debug(
          String.format("Forming UPDATE SQL statements completed successfully: %d statement(s)",
              sqlStatements.size()));
    }

    // INSERT SQL statements
    final List<Job> insertJobs = getJobsSubtraction(jobsFromXml, jobsFromDB);
    insertJobs.forEach((x) -> DBUtil.addJob(x.getDepcode(), x.getDepjob(), x.getDescription()));
    if (logger.isDebugEnabled()) {
      logger.debug(
          String.format("Forming INSERT SQL statements completed successfully: %d statement(s)",
              insertJobs.size()));
    }

    // DELETE SQL statements
    final List<Job> deleteJobs = getJobsSubtraction(jobsFromDB, jobsFromXml);
    deleteJobs.forEach((db) -> DBUtil.deleteJob(db.getId()));
    if (logger.isDebugEnabled()) {
      logger.debug(
          String.format("Forming DELETE SQL statements completed successfully: %d statement(s)",
              deleteJobs.size()));
    }
  }

  /**
   * Performs subtraction of one {@link HashSet} of {@link Job} objects from another in other words
   * HashSet1 - HashSet2 = HashSet3
   *
   * @param jobs1 HashSet1
   * @param jobs2 HashSet2
   * @return new {@link HashSet} which represents subtraction of HashSet2 from HashSet1
   */
  private static List<Job> getJobsSubtraction(final List<Job> jobs1, final List<Job> jobs2) {
    final List<Job> result = new ArrayList<>();
    jobs1.forEach((job1) -> {
      if (jobs2.stream().noneMatch((job2) -> (job2.getDepcode().equals(job1.getDepcode())
          && job2.getDepjob().equals(job1.getDepjob())))) {
        result.add(new Job() {
          {
            setId(job1.getId());
            setDepcode(job1.getDepcode());
            setDepjob(job1.getDepjob());
            setDescription(job1.getDescription());
          }
        });
      }
    });
    return result;
  }

  @Override
  protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
    return application.sources(Main.class);
  }

}
