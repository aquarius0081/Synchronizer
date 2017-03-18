package com.company;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;

/**
 *
 */
public class XMLUtil {
    static HashSet<Job> readFromXml(String path) throws ParserConfigurationException, SAXException, IOException {
        File inXml = new File(path);
        DocumentBuilderFactory dbFactory
                = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(inXml);
        Element jobsElement = doc.getDocumentElement();
        HashSet<Job> jobsFromXml = new HashSet<>();
        System.out.println("Root element :"
                + doc.getDocumentElement().getNodeName());
        NodeList jobNodes = jobsElement.getElementsByTagName("Job");
        for (int i = 0; i < jobNodes.getLength(); i++) {
            System.out.println(jobNodes.item(i).getNodeName());
            NodeList columnNodes = jobNodes.item(i).getChildNodes();
            Job job = new Job();
            for (int j = 0; j < columnNodes.getLength(); j++) {
                final String columnNodeName = columnNodes.item(j).getNodeName();
                String columnNodeText = columnNodes.item(j).getTextContent();
                if (columnNodeName.equalsIgnoreCase("depcode")) {
                    job.setDepcode(columnNodeText);
                    System.out.println(columnNodes.item(j).getNodeName() + " " + columnNodes.item(j).getTextContent());
                } else if (columnNodeName.equalsIgnoreCase("depjob")) {
                    job.setDepjob(columnNodeText);
                    System.out.println(columnNodes.item(j).getNodeName() + " " + columnNodes.item(j).getTextContent());
                } else if (columnNodeName.equalsIgnoreCase("description")) {
                    job.setDescription(columnNodeText);
                    System.out.println(columnNodes.item(j).getNodeName() + " " + columnNodes.item(j).getTextContent());
                }
            }
            if (jobsFromXml.stream().anyMatch((j)->(j.getDepcode().equals(job.getDepcode())) && (j.getDepjob().equals(job.getDepjob())))) {
                throw new RuntimeException("XML file contains duplicate keys!");
            }
            jobsFromXml.add(job);
        }
        return jobsFromXml;
    }
}
