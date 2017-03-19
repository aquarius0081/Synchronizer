package com.company;

import org.apache.log4j.Logger;
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
    private final static Logger logger = Logger.getLogger(DBUtil.class);

    static HashSet<Job> readFromXml(final String path) {
        File inXml = new File(path);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        HashSet<Job> jobsFromXml = new HashSet<>();
        try {
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inXml);
            Element jobsElement = doc.getDocumentElement();

            NodeList jobNodes = jobsElement.getElementsByTagName("Job");
            for (int i = 0; i < jobNodes.getLength(); i++) {
                NodeList columnNodes = jobNodes.item(i).getChildNodes();
                Job job = new Job();
                for (int j = 0; j < columnNodes.getLength(); j++) {
                    final String columnNodeName = columnNodes.item(j).getNodeName();
                    String columnNodeText = columnNodes.item(j).getTextContent();
                    if (columnNodeName.equalsIgnoreCase("depcode")) {
                        job.setDepcode(columnNodeText);
                    } else if (columnNodeName.equalsIgnoreCase("depjob")) {
                        job.setDepjob(columnNodeText);
                    } else if (columnNodeName.equalsIgnoreCase("description")) {
                        job.setDescription(columnNodeText);
                    }
                }
                if (jobsFromXml.stream().anyMatch((j) -> (j.getDepcode().equals(job.getDepcode())) && (j.getDepjob().equals(job.getDepjob())))) {
                    String fatalErrorMessage = "XML file contains duplicate keys!";
                    logger.fatal(fatalErrorMessage);
                    throw new RuntimeException(fatalErrorMessage);
                }
                jobsFromXml.add(job);
            }
        } catch (ParserConfigurationException e) {
            String fatalErrorMessage = String.format("Fatal error during building new document for XML file: %s", e.getStackTrace());
            logger.fatal(fatalErrorMessage);
            throw new RuntimeException(fatalErrorMessage);
        } catch (SAXException e) {
            String fatalErrorMessage = String.format("Fatal error during parsing XML file: %s", e.getStackTrace());
            logger.fatal(fatalErrorMessage);
            throw new RuntimeException(fatalErrorMessage);
        } catch (IOException e) {
            String fatalErrorMessage = "XML file is not found!";
            logger.fatal(fatalErrorMessage);
            throw new RuntimeException(fatalErrorMessage);
        }
        return jobsFromXml;
    }
}
