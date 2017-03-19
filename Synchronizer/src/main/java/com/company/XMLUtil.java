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
class XMLUtil {
    private final static Logger logger = Logger.getLogger(DBUtil.class);

    static HashSet<Job> readFromXml(final String path) {
        final File inXml = new File(path);
        final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        final HashSet<Job> jobsFromXml = new HashSet<>();
        try {
            final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            final Document doc = dBuilder.parse(inXml);
            final Element jobsElement = doc.getDocumentElement();

            final NodeList jobNodes = jobsElement.getElementsByTagName("Job");
            for (int i = 0; i < jobNodes.getLength(); i++) {
                final NodeList columnNodes = jobNodes.item(i).getChildNodes();
                final Job job = new Job();
                for (int j = 0; j < columnNodes.getLength(); j++) {
                    final String columnNodeName = columnNodes.item(j).getNodeName();
                    final String columnNodeText = columnNodes.item(j).getTextContent();
                    if (columnNodeName.equalsIgnoreCase("depcode")) {
                        job.setDepcode(columnNodeText);
                    } else if (columnNodeName.equalsIgnoreCase("depjob")) {
                        job.setDepjob(columnNodeText);
                    } else if (columnNodeName.equalsIgnoreCase("description")) {
                        job.setDescription(columnNodeText);
                    }
                }
                if (jobsFromXml.stream().anyMatch((j) -> (j.getDepcode().equals(job.getDepcode())) && (j.getDepjob().equals(job.getDepjob())))) {
                    final String fatalErrorMessage = "XML file contains duplicate keys!";
                    logger.fatal(fatalErrorMessage);
                    throw new RuntimeException(fatalErrorMessage);
                }
                jobsFromXml.add(job);
            }
        } catch (ParserConfigurationException e) {
            final String fatalErrorMessage = String.format("Fatal error during building new document for XML file: %s", e.getMessage());
            logger.fatal(fatalErrorMessage);
            throw new RuntimeException(fatalErrorMessage);
        } catch (SAXException e) {
            final String fatalErrorMessage = String.format("Fatal error during parsing XML file: %s", e.getMessage());
            logger.fatal(fatalErrorMessage);
            throw new RuntimeException(fatalErrorMessage);
        } catch (IOException e) {
            final String fatalErrorMessage = "XML file is not found!";
            logger.fatal(fatalErrorMessage);
            throw new RuntimeException(fatalErrorMessage);
        }
        return jobsFromXml;
    }
}
