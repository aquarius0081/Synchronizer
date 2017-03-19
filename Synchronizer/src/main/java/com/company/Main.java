package com.company;

import org.apache.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Main {
    private final static Logger logger = Logger.getLogger(Main.class);

    private static ThreadLocal<Marshaller> marshaller = new ThreadLocal<Marshaller>() {
        @Override
        protected Marshaller initialValue() {
            JAXBContext jaxbContext = null;
            try {
                jaxbContext = JAXBContext.newInstance(Jobs.class);
                Marshaller marshaller = jaxbContext.createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                return marshaller;
            } catch (JAXBException e) {
                e.printStackTrace();
            }
            return null;
        }
    };

    public static void main(String[] args) {
        final String paramsErrorMessage = "Provided incorrect parameters! Parameters usage: export|sync fileName.xml";
        if (args.length != 2) {
            System.out.println(paramsErrorMessage);
            return;
        } else if (!args[0].equalsIgnoreCase("sync") && !args[0].equalsIgnoreCase("export")) {
            System.out.println(paramsErrorMessage);
            return;
        }
        if (args[0].equalsIgnoreCase("export")) {
            try {
                logger.info("Start export process from DB to XML file.");
                exportToXml(args[1]);
                String successMessage = "Export process from DB to XML file completed successfully.";
                logger.info(successMessage);
                System.out.println(successMessage);
            } catch (Exception e) {
                logger.fatal("Export process from DB to XML file failed!");
                System.out.println("Export process from DB to XML file failed! Please see log for details.");
            }
        } else if (args[0].equalsIgnoreCase("sync")) {
            try {
                logger.info("Start synchronization process from XML file to DB.");
                syncFromXml(args[1]);
                String successMessage = "Synchronization process from XML file to DB completed successfully.";
                logger.info(successMessage);
                System.out.println(successMessage);
            } catch (Exception e) {
                String fatalErrorMessage = "Synchronization process from XML file to DB failed! Please see log for details.";
                logger.fatal(fatalErrorMessage);
                System.out.println(fatalErrorMessage);
            }
        }
    }

    private static void exportToXml(final String path) {
        if(logger.isDebugEnabled()){
            logger.debug("Start read data from DB.");
        }
        HashSet<Job> jobsList = DBUtil.readFromDB();
        if(logger.isDebugEnabled()){
            logger.debug("Reading data from DB completed successfully.");
        }
        Jobs jobs = new Jobs();
        jobs.setJobs(jobsList);
        File outXml = new File(path);

        if(logger.isDebugEnabled()){
            logger.debug("Start marshalling DB data to XML file.");
        }
        try {
            marshaller.get().marshal(jobs, outXml);
        } catch (JAXBException e) {
            logger.fatal(String.format("Fatal error during marshalling DB data to XML file: %s", e.getStackTrace()));
            throw new RuntimeException("Fatal error during marshalling DB data to XML file");
        }
        if(logger.isDebugEnabled()){
            logger.debug("Marshalling DB data to XML file completed successfully.");
        }
    }

    private static void syncFromXml(final String path) {
        if(logger.isDebugEnabled()){
            logger.debug("Start read data from XML file.");
        }
        HashSet<Job> jobsFromXml = XMLUtil.readFromXml(path);
        if(logger.isDebugEnabled()){
            logger.debug("Reading data from XML file completed successfully.");
            logger.debug("Start read data from DB.");
        }
        HashSet<Job> jobsFromDB = DBUtil.readFromDB();
        if(logger.isDebugEnabled()){
            logger.debug("Reading data from DB completed successfully.");
            logger.debug("Start forming SQL statements to sync DB data from XML file.");
        }
        List<String> sqlStatements = new ArrayList<>();

        //UPDATE SQL statements
        jobsFromXml.forEach((x) -> {
            jobsFromDB.forEach((d) -> {
                if (d.getDepcode().equals(x.getDepcode()) &&
                        d.getDepjob().equals(x.getDepjob()) &&
                        !d.getDescription().equals(x.getDescription())) {
                    sqlStatements.add(
                            String.format(
                                    "UPDATE [Enterprise].[dbo].[Job] SET [Description] = '%s' WHERE DepCode = '%s' AND DepJob = '%s'",
                                    x.getDescription(),
                                    d.getDepcode(),
                                    d.getDepjob()));
                }
            });
        });
        if(logger.isDebugEnabled()){
            logger.debug(String.format("Forming UPDATE SQL statements completed successfully: %d statement(s)", sqlStatements.size()));
        }

        //INSERT SQL statements
        HashSet<Job> insertJobs = getJobsSubtraction(jobsFromXml, jobsFromDB);
        insertJobs.forEach((x) -> {
            sqlStatements.add(
                    String.format(
                            "INSERT INTO [Enterprise].[dbo].[Job] ([DepCode], [DepJob], [Description]) VALUES ('%s','%s','%s')",
                            x.getDepcode(),
                            x.getDepjob(),
                            x.getDescription()));
        });
        if(logger.isDebugEnabled()){
            logger.debug(String.format("Forming INSERT SQL statements completed successfully: %d statement(s)", insertJobs.size()));
        }

        //DELETE SQL statements
        HashSet<Job> deleteJobs = getJobsSubtraction(jobsFromDB, jobsFromXml);
        deleteJobs.forEach((db) -> {
            sqlStatements.add(
                    String.format(
                            "DELETE FROM [Enterprise].[dbo].[Job] WHERE DepCode = '%s' AND DepJob = '%s'",
                            db.getDepcode(),
                            db.getDepjob()));
        });
        if(logger.isDebugEnabled()){
            logger.debug(String.format("Forming DELETE SQL statements completed successfully: %d statement(s)", deleteJobs.size()));
        }

        if (!sqlStatements.isEmpty()) {
            if(logger.isDebugEnabled()){
                logger.debug("Start sync DB data from XML file.");
            }
            DBUtil.writeToDB(sqlStatements);
            if(logger.isDebugEnabled()){
                logger.debug("Sync DB data from XML file completed successfully.");
            }
        } else {
            if(logger.isInfoEnabled()){
                logger.info("Nothing to sync!");
            }
        }
    }

    private static HashSet<Job> getJobsSubtraction(HashSet<Job> jobs1, HashSet<Job> jobs2) {
        HashSet<Job> result = new HashSet<>();
        jobs1.forEach((job1) -> {
            if (jobs2.stream().noneMatch((job2) ->
                    (job2.getDepcode().equals(job1.getDepcode()) &&
                            job2.getDepjob().equals(job1.getDepjob())))) {
                result.add(new Job() {{
                    setDepcode(job1.getDepcode());
                    setDepjob(job1.getDepjob());
                    setDescription(job1.getDescription());
                }});
            }
        });
        return result;
    }

}
