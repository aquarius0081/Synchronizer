package com.company;

import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Main {

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

    private static ThreadLocal<Unmarshaller> unmarshaller = new ThreadLocal<Unmarshaller>() {
        @Override
        protected Unmarshaller initialValue() {
            JAXBContext jaxbContext = null;
            try {
                jaxbContext = JAXBContext.newInstance(Jobs.class);
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                return unmarshaller;
            } catch (JAXBException e) {
                e.printStackTrace();
            }
            return null;
        }
    };

    public static void main(String[] args) throws JAXBException, IOException, SAXException, ParserConfigurationException {
        exportToXml();
        syncFromXml();
    }

    private static void exportToXml() throws JAXBException {
        HashSet<Job> jobsList = DBUtil.readFromDB();
        Jobs jobs = new Jobs();
        jobs.setJobs(jobsList);
        File outXml = new File("temp/exportedXml.xml");
        for (Job job : jobs.getJobs()) {
            System.out.println("DepCode: " + job.getDepcode() + " DepJob: " + job.getDepjob() + " Description: " + job.getDescription());
        }

        marshaller.get().marshal(jobs, outXml);
    }

    private static void syncFromXml() throws ParserConfigurationException, IOException, SAXException {
        HashSet<Job> jobsFromXml = XMLUtil.readFromXml("temp/importXml.xml");
        HashSet<Job> jobsFromDB = DBUtil.readFromDB();
        List<String> sqlStatements = new ArrayList<>();

        //Update
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

        //Insert
        HashSet<Job> insertJobs = getJobsSubtraction(jobsFromXml, jobsFromDB);
        insertJobs.forEach((x) -> {
            sqlStatements.add(
                    String.format(
                            "INSERT INTO [Enterprise].[dbo].[Job] ([DepCode], [DepJob], [Description]) VALUES ('%s','%s','%s')",
                            x.getDepcode(),
                            x.getDepjob(),
                            x.getDescription()));
        });

        //Delete
        HashSet<Job> deleteJobs = getJobsSubtraction(jobsFromDB, jobsFromXml);
        deleteJobs.forEach((db) -> {
            sqlStatements.add(
                    String.format(
                            "DELETE FROM [Enterprise].[dbo].[Job] WHERE DepCode = '%s' AND DepJob = '%s'",
                            db.getDepcode(),
                            db.getDepjob()));
        });

        sqlStatements.forEach((s) -> System.out.println(s));
        if (!sqlStatements.isEmpty()) {
            DBUtil.writeToDB(sqlStatements);
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
