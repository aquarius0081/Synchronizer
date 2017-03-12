package com.company;

import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) throws JAXBException {
        List<Job> jobsList = DBUtil.readFromDB();
        Jobs jobs = new Jobs();
        jobs.setJobs(jobsList);
        File outXml = new File("exportedXml.xml");
        for (Job job : jobs.getJobs()) {
            System.out.println("DepCode: " + job.getDepcode() + " DepJob: " + job.getDepjob() + " Description: " + job.getDescription());
        }
        JAXBContext jaxbContext = JAXBContext.newInstance(Jobs.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,true);
        marshaller.marshal(jobs, outXml);

    }
}
