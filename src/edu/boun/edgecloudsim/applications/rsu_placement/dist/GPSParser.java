package edu.boun.edgecloudsim.applications.rsu_placement.dist;

import edu.boun.edgecloudsim.applications.rsu_placement.utils.FileUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class GPSParser {

    public static void main(String[] args) throws IOException {
        String inputFile = "/Users/wonderwall/Desktop/gsu/EdgeCloudSim/scripts/rsu_placement/input/traffic2500.xml";
        List<Coordinate> coordinates = new ArrayList<>();

        try {
            NodeList timeStepNodeList = FileUtils.readTimeStepListFromTrafficData(inputFile);
            for (int i = 0; i < timeStepNodeList.getLength(); i++) {
                Element timeStep = (Element) timeStepNodeList.item(i);
                NodeList vehicleNodeList = timeStep.getElementsByTagName("vehicle");
                for (int j = 0; j < vehicleNodeList.getLength(); j++) {
                    Element vehicle = (Element) vehicleNodeList.item(j);
                    Double lat = Double.parseDouble(vehicle.getAttribute("y"));
                    Double lng = Double.parseDouble(vehicle.getAttribute("x"));
                    Coordinate coordinate = new Coordinate(lat, lng);
                    coordinates.add(coordinate);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<String> lines = coordinates.stream()
                .map(Objects::toString)
                .collect(toList());
        Path file = Paths.get("/Users/wonderwall/Desktop/gsu/siu/py/coordinates.txt");
        Files.write(file, lines, Charset.forName("UTF-8"));
    }

    public static class Coordinate {
        private double lat;
        private double lng;

        public Coordinate(double lat, double lng) {
            this.lat = lat;
            this.lng = lng;
        }

        @Override
        public String toString() {
            return lat + "," + lng;
        }
    }

}
