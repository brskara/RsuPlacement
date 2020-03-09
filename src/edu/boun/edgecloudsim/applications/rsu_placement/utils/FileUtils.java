package edu.boun.edgecloudsim.applications.rsu_placement.utils;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

public class FileUtils {

    public static NodeList readTimeStepListFromTrafficData(String filePath) throws ParserConfigurationException, IOException, SAXException {
        Document doc;
        File trafficFile = new File(filePath);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        doc = dBuilder.parse(trafficFile);
        doc.getDocumentElement().normalize();
        return doc.getElementsByTagName("timestep");
    }

    public static NodeList readRSULocationsFromRSUConfig(String filePath) throws ParserConfigurationException, IOException, SAXException {
        Document doc;
        File file = new File(filePath);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        doc = dBuilder.parse(file);
        doc.getDocumentElement().normalize();
        return doc.getElementsByTagName("location");
    }
}
