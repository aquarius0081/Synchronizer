package com.company;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
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

    public static void main(String[] args) throws JAXBException {
        exportToXml();
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

    private static void syncFromXml() throws JAXBException {
        File inXml = new File("temp/exportedXml.xml");
        Jobs jobs = (Jobs) unmarshaller.get().unmarshal(inXml);

        for (Job job : jobs.getJobs()) {
            System.out.println("DepCode: " + job.getDepcode() + " DepJob: " + job.getDepjob() + " Description: " + job.getDescription());
        }
    }
}
