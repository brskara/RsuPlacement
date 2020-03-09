package edu.boun.edgecloudsim.applications.rsu_placement.dist;

import edu.boun.edgecloudsim.applications.rsu_placement.model.Coordinate;
import edu.boun.edgecloudsim.applications.rsu_placement.utils.CoordinateUtils;
import edu.boun.edgecloudsim.applications.rsu_placement.utils.FileUtils;
import edu.wlu.cs.levy.CG.KDTree;
import edu.wlu.cs.levy.CG.KeyDuplicateException;
import edu.wlu.cs.levy.CG.KeySizeException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.*;

public class CellLoadCalculator {

    private static final double CAPACITY = 5; //traffic35.xml
    private static final String TRAFFIC_INPUT = "scripts/rsu_placement/input/traffic3500.xml";
    private static final String RSU_INPUT = "scripts/rsu_placement/config/uniform_rsu.xml";
    private static List<Coordinate> vehicleCoordinates = new ArrayList<>();
//    private static List<Coordinate> rsuCoordinates = new ArrayList<>();
    private static KDTree<Integer> kdTree = new KDTree<>(2);
    private static int[] cells = new int[100];
    private static double[] numberOfRSUs = new double[100];

    public static void main(String[] args) {
        Map<Integer, Long> resultMap = new HashMap<>();
        readRSUs();
        readTasks();
        for(int i = 0; i < cells.length; i++){
            double noOfRSU = cells[i] / CAPACITY;
            numberOfRSUs[i] = noOfRSU;
            resultMap.put(i, Math.round(numberOfRSUs[i]));
        }
//        System.out.println(sortByValue(resultMap));
        System.out.println(resultMap);
    }

    private static void readRSUs() {
        try {
            NodeList locationList = FileUtils.readRSULocationsFromRSUConfig(RSU_INPUT);
            for (int i = 0; i < locationList.getLength(); i++) {
                Element location = (Element) locationList.item(i);
                Double lat = Double.parseDouble(location.getChildNodes().item(3).getTextContent());
                Double lng = Double.parseDouble(location.getChildNodes().item(1).getTextContent());
                addToKDTree(lat, lng, i);
//                rsuCoordinates.add(new Coordinate(lat, lng));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void addToKDTree(Double lat, Double lng, int i) {
        double[] rsuPosition = {CoordinateUtils.lon2x(lng), CoordinateUtils.lat2y(lat)};
        try {
            kdTree.insert(rsuPosition, i);
        } catch (KeySizeException | KeyDuplicateException e) {
            e.printStackTrace();
        }
    }


    private static void readTasks() {
        Double northernBound = 51.54527d;
        Double southernBound = 51.51833d;
        Double westernBound = -0.12805d;
        Double easternBound = -0.08472d;
        try {
            NodeList timeStepNodeList = FileUtils.readTimeStepListFromTrafficData(TRAFFIC_INPUT);
            for (int i = 0; i < timeStepNodeList.getLength(); i++) {
                Element timeStep = (Element) timeStepNodeList.item(i);
                NodeList vehicleNodeList = timeStep.getElementsByTagName("vehicle");
                int[] load = new int[100];
                for (int j = 0; j < vehicleNodeList.getLength(); j++) {
                    Element vehicle = (Element)vehicleNodeList.item(j);
                    Double lat = Double.parseDouble(vehicle.getAttribute("y"));
                    Double lng = Double.parseDouble(vehicle.getAttribute("x"));
                    if(lat > northernBound || lat < southernBound || lng > easternBound || lng < westernBound){
                        continue;
                    }
                    int id = findRSUToAssign(new Coordinate(lat, lng));
//                    cells[id]++;
                    load[id]++;
                }
                for (int k = 0; k < cells.length; k++) {
                    if(load[k] > cells[k]){
                        cells[k] = load[k];
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Integer findRSUToAssign(Coordinate coordinate) {
        double[] position = {CoordinateUtils.lon2x(coordinate.getLng()), CoordinateUtils.lat2y(coordinate.getLat())};
        Integer id = -1;
        try {
            id = kdTree.nearest(position);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return id;
    }


    private static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }


}
