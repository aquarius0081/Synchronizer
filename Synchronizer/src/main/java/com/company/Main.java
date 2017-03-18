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
        //exportToXml();
        syncFromXml();
    }

    private static void exportToXml() throws JAXBException {
        List<Job> jobsList = DBUtil.readFromDB();
        Jobs jobs = new Jobs();
        jobs.setJobs(jobsList);
        File outXml = new File("temp/exportedXml.xml");
        for (Job job : jobs.getJobs()) {
            System.out.println("DepCode: " + job.getDepcode() + " DepJob: " + job.getDepjob() + " Description: " + job.getDescription());
        }

        marshaller.get().marshal(jobs, outXml);
    }

    private static void syncFromXml() throws ParserConfigurationException, IOException, SAXException {
        List jobsFromXml = XMLUtil.readFromXml();
        List jobsFromDB = DBUtil.readFromDB();
        List<String> sqlStatements = new ArrayList<String>();

        //Update
        jobsFromXml.forEach((x) -> {
            jobsFromDB.forEach((d) -> {
                if (((Job) d).getDepcode().equals(((Job) x).getDepcode()) &&
                        ((Job) d).getDepjob().equals(((Job) x).getDepjob()) &&
                        !((Job) d).getDescription().equals(((Job) x).getDescription())) {
                    sqlStatements.add("UPDATE [Enterprise].[dbo].[Job] SET [Description] = ? WHERE DepCode = ? AND DepJob = ?");
                }
            });
        });

        //Insert
        jobsFromXml.removeIf((x) -> jobsFromDB.stream().anyMatch((d) ->
            (((Job) d).getDepcode().equals(((Job) x).getDepcode()) &&
                    ((Job) d).getDepjob().equals(((Job) x).getDepjob()))
        ));
        jobsFromXml.forEach((x) -> {
            sqlStatements.add("INSERT INTO [Enterprise].[dbo].[Job] ([DepCode], [DepJob], [Description]) VALUES (?,?,?)");
        });

        //Delete
        jobsFromDB.removeIf((db) -> jobsFromXml.stream().anyMatch((xml) ->
                (((Job) xml).getDepcode().equals(((Job) db).getDepcode()) &&
                        ((Job) xml).getDepjob().equals(((Job) db).getDepjob()))
        ));
        jobsFromDB.forEach((db) -> {
            sqlStatements.add("DELETE FROM [Enterprise].[dbo].[Job] WHERE DepCode = ? AND DepJob = ?");
        });

        sqlStatements.forEach((s) -> System.out.println(s));
    }

}
