package edu.boun.edgecloudsim.applications.rsu_placement;

import edu.boun.edgecloudsim.applications.rsu_placement.utils.FileUtils;
import edu.boun.edgecloudsim.core.SimSettings;
import edu.boun.edgecloudsim.task_generator.LoadGeneratorModel;
import edu.boun.edgecloudsim.utils.SimLogger;
import edu.boun.edgecloudsim.utils.TaskProperty;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class TrafficLoadGenerator extends LoadGeneratorModel {

    private String inputFile;
    private TreeMap<Double, List<Integer>> timeVehicleMap;

    public TrafficLoadGenerator(String trafficScenarioFile) {
        super();
        this.inputFile = trafficScenarioFile;
    }

    @Override
    public void initializeModel() {
        int numberOfSkippedTasks = 0;
        taskList = new ArrayList<>();
        timeVehicleMap = new TreeMap<>();
        //exponential number generator for file input size, file output size and task length
        ExponentialDistribution[] expRngList = new ExponentialDistribution[3];
        expRngList[0] = new ExponentialDistribution(SimSettings.getInstance().getTaskLookUpTable()[0][5]);
        expRngList[1] = new ExponentialDistribution(SimSettings.getInstance().getTaskLookUpTable()[0][6]);
        expRngList[2] = new ExponentialDistribution(SimSettings.getInstance().getTaskLookUpTable()[0][7]);
        Double northernBound = SimSettings.getInstance().getNorthernBound();
        Double southernBound = SimSettings.getInstance().getSouthernBound();
        Double westernBound = SimSettings.getInstance().getWesternBound();
        Double easternBound = SimSettings.getInstance().getEasternBound();
        try {
            NodeList timeStepNodeList = FileUtils.readTimeStepListFromTrafficData(inputFile);
            for (int i = 0; i < timeStepNodeList.getLength(); i++) {
                Element timeStep = (Element) timeStepNodeList.item(i);
                Double time = Double.parseDouble(timeStep.getAttribute("time"));
                if(time < SimSettings.getInstance().getWarmUpPeriod()){
                    continue;
                }
                NodeList vehicleNodeList = timeStep.getElementsByTagName("vehicle");
                List<Integer> vehicleIdList = new ArrayList<>();
                for (int j = 0; j < vehicleNodeList.getLength(); j++) {
                    Element vehicle = (Element) vehicleNodeList.item(j);
                    Double lat = Double.parseDouble(vehicle.getAttribute("y"));
                    Double lng = Double.parseDouble(vehicle.getAttribute("x"));
                    if(lat > northernBound || lat < southernBound || lng > easternBound || lng < westernBound){
                        numberOfSkippedTasks++;
                        continue;
                    }

                    Integer vehicleId = Integer.parseInt(vehicle.getAttribute("id"));
                    taskList.add(new TaskProperty(vehicleId, time, expRngList));
//                    coordinates.add(new TaskProperty(vehicleId, time, SimSettings.getInstance().getTaskLookUpTable()[0][5],
//                            SimSettings.getInstance().getTaskLookUpTable()[0][6], SimSettings.getInstance().getTaskLookUpTable()[0][7]));
                    vehicleIdList.add(vehicleId);
                }
                timeVehicleMap.put(time, vehicleIdList);
            }
            SimLogger.getInstance().setNumberOfSkippedTasks(numberOfSkippedTasks);
        } catch (Exception e) {
            SimLogger.printLine("Traffic input file cannot be parsed! Terminating simulation...");
            e.printStackTrace();
            System.exit(0);
        }
    }

    public List<Integer> getVehicleIdListByTime(double time){
        return timeVehicleMap.floorEntry(time).getValue();
    }

    @Override
    public int getTaskTypeOfDevice(int deviceId) {
        return 0;
    }
}
