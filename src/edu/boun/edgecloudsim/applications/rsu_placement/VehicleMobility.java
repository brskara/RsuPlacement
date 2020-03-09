package edu.boun.edgecloudsim.applications.rsu_placement;

import edu.boun.edgecloudsim.applications.rsu_placement.model.Coordinate;
import edu.boun.edgecloudsim.applications.rsu_placement.utils.FileUtils;
import edu.boun.edgecloudsim.mobility.MobilityModel;
import edu.boun.edgecloudsim.utils.SimLogger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.TreeMap;

public class VehicleMobility extends MobilityModel {

    private String inputFile;
    private HashMap<Integer, TreeMap<Double, Coordinate>> vehicleLocations; //<vehicleId, <time, coordinate>>

    public VehicleMobility(String inputFile) {
        super();
        this.inputFile = inputFile;
    }

    @Override
    public void initialize() {
        vehicleLocations = new HashMap<>();
        try {
            NodeList timeStepNodeList = FileUtils.readTimeStepListFromTrafficData(inputFile);
            for (int i = 0; i < timeStepNodeList.getLength(); i++) {
                Element timeStep = (Element) timeStepNodeList.item(i);
                Double time = Double.parseDouble(timeStep.getAttribute("time"));
                NodeList vehicleNodeList = timeStep.getElementsByTagName("vehicle");
                for (int j = 0; j < vehicleNodeList.getLength(); j++) {
                    Element vehicle = (Element) vehicleNodeList.item(j);
                    Integer vehicleId = Integer.parseInt(vehicle.getAttribute("id"));
                    Double lat = Double.parseDouble(vehicle.getAttribute("y"));
                    Double lng = Double.parseDouble(vehicle.getAttribute("x"));
                    Coordinate coordinate = new Coordinate(lat, lng);
                    TreeMap<Double, Coordinate> timeCoordinateMap;
                    if(vehicleLocations.containsKey(vehicleId)){
                        timeCoordinateMap = vehicleLocations.get(vehicleId);
                        timeCoordinateMap.put(time, coordinate);
                    } else {
                        timeCoordinateMap = new TreeMap<>();
                        timeCoordinateMap.put(time, coordinate);
                        vehicleLocations.put(vehicleId, timeCoordinateMap);
                    }
                }
            }
        } catch (Exception e) {
            SimLogger.printLine("Traffic input file cannot be parsed! Terminating simulation...");
            e.printStackTrace();
            System.exit(0);
        }
    }

    @Override
    public Coordinate getLocation(int vehicleId, double time) {
        return vehicleLocations.get(vehicleId).floorEntry(time).getValue();
    }
}
