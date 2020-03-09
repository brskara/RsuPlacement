/*
 * Title:        EdgeCloudSim - Simulation Logger
 *
 * Description:
 * SimLogger is responsible for storing simulation events/results
 * in to the files in a specific format.
 * Format is decided in a way to use results in matlab efficiently.
 * If you need more results or another file format, you should modify
 * this class.
 *
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 * Copyright (c) 2017, Bogazici University, Istanbul, Turkey
 */

package edu.boun.edgecloudsim.utils;

import edu.boun.edgecloudsim.applications.rsu_placement.TrafficLoadGenerator;
import edu.boun.edgecloudsim.applications.rsu_placement.model.Coordinate;
import edu.boun.edgecloudsim.core.SimManager;
import edu.boun.edgecloudsim.core.SimSettings;
import edu.boun.edgecloudsim.core.SimSettings.NETWORK_DELAY_TYPES;
import edu.boun.edgecloudsim.mobility.MobilityModel;
import edu.boun.edgecloudsim.utils.SimLogger.NETWORK_ERRORS;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class SimLogger {

    public static enum TASK_STATUS {
        CREATED, UPLOADING, PROCESSING, DOWNLOADING, COMLETED, REJECTED_DUE_TO_VM_CAPACITY, REJECTED_DUE_TO_BANDWIDTH,
        UNFINISHED_DUE_TO_BANDWIDTH, UNFINISHED_DUE_TO_MOBILITY, REJECTED_DUE_TO_NOT_IN_NETWORK_RANGE
    }

    public static enum NETWORK_ERRORS {
        LAN_ERROR, MAN_ERROR, WAN_ERROR, NONE
    }

    private static boolean fileLogEnabled;
    private static boolean printLogEnabled;
    private String filePrefix;
    private String outputFolder;
    private Map<Integer, LogItem> taskMap;
    private LinkedList<VmLoadLogItem> vmLoadList;
    private int numberOfSkippedTasks;

    private static SimLogger singleton = new SimLogger();

    /*
     * A private Constructor prevents any other class from instantiating.
     */
    private SimLogger() {
        fileLogEnabled = false;
        printLogEnabled = false;
    }

    /* Static 'instance' method */
    public static SimLogger getInstance() {
        return singleton;
    }

    public static void enableFileLog() {
        fileLogEnabled = true;
    }

    public static void enablePrintLog() {
        printLogEnabled = true;
    }

    public static boolean isFileLogEnabled() {
        return fileLogEnabled;
    }

    public static void disablePrintLog() {
        printLogEnabled = false;
    }

    private void appendToFile(BufferedWriter bw, String line) throws IOException {
        bw.write(line);
        bw.newLine();
    }

    public static void printLine(String msg) {
        if (printLogEnabled)
            System.out.println(msg);
    }

    public static void print(String msg) {
        if (printLogEnabled)
            System.out.print(msg);
    }

    public void simStarted(String outFolder, String fileName) {
        filePrefix = fileName;
        outputFolder = outFolder;
        taskMap = new HashMap<Integer, LogItem>();
        vmLoadList = new LinkedList<VmLoadLogItem>();
    }

    public void addLog(int taskId, int taskType, int taskLenght, int taskInputSize, int taskOutputSize) {
        // printLine(taskId+"->"+taskStartTime);
        taskMap.put(taskId, new LogItem(taskType, taskLenght, taskInputSize, taskOutputSize));
    }

    public void taskStarted(int taskId, double time) {
        taskMap.get(taskId).taskStarted(time);
    }

    public void setUploadDelay(int taskId, double delay, NETWORK_DELAY_TYPES delayType) {
        taskMap.get(taskId).setUploadDelay(delay, delayType);
    }

    public void setDownloadDelay(int taskId, double delay, NETWORK_DELAY_TYPES delayType) {
        taskMap.get(taskId).setDownloadDelay(delay, delayType);
    }

    public void taskAssigned(int taskId, int datacenterId, int hostId, int vmId, int vmType) {
        taskMap.get(taskId).taskAssigned(datacenterId, hostId, vmId, vmType);
    }

    public void taskExecuted(int taskId) {
        taskMap.get(taskId).taskExecuted();
    }

    public void taskEnded(int taskId, double time) {
        taskMap.get(taskId).taskEnded(time);
    }

    public void rejectedDueToVMCapacity(int taskId, double time, int vmType) {
        taskMap.get(taskId).taskRejectedDueToVMCapacity(time, vmType);
    }

    public void rejectedDueToVMCapacity(int taskId, int hostId, double time, int vmType) {
        taskMap.get(taskId).taskRejectedDueToVMCapacity(time, hostId, vmType);
    }

    public void rejectedDueToNotInNetworkRange(int taskId, double time) {
        taskMap.get(taskId).rejectedDueToNotInNetworkRange(time);
    }

    public void rejectedDueToBandwidth(int taskId, double time, int vmType, NETWORK_DELAY_TYPES delayType) {
        taskMap.get(taskId).taskRejectedDueToBandwidth(time, vmType, delayType);
    }

    public void failedDueToBandwidth(int taskId, double time, NETWORK_DELAY_TYPES delayType) {
        taskMap.get(taskId).taskFailedDueToBandwidth(time, delayType);
    }

    public void failedDueToMobility(int taskId, double time) {
        taskMap.get(taskId).taskFailedDueToMobility(time);
    }

    public void addVmUtilizationLog(double time, double[] loadOnEdge) {
        vmLoadList.add(new VmLoadLogItem(time, loadOnEdge));
    }

    public void simStopped() throws IOException {
        File successFile, failFile, vmLoadFile, genericFile, taskDistFile;
        FileWriter successFW, failFW, vmLoadFW, genericFW, taskDistFW;
        BufferedWriter successBW = null, failBW = null, vmLoadBW = null, genericBW = null, taskDistBW = null;

        // open all files and prepare them for write
        if (fileLogEnabled) {
            if (SimSettings.getInstance().getDeepFileLoggingEnabled()) {
                successFile = new File(outputFolder, filePrefix + "_SUCCESS.log");
                successFW = new FileWriter(successFile, true);
                successBW = new BufferedWriter(successFW);
                appendToFile(successBW, "#auto generated file!");

                failFile = new File(outputFolder, filePrefix + "_FAIL.log");
                failFW = new FileWriter(failFile, true);
                failBW = new BufferedWriter(failFW);
                appendToFile(failBW, "#auto generated file!");
            }

            vmLoadFile = new File(outputFolder, filePrefix + "_VM_LOAD.log");
            vmLoadFW = new FileWriter(vmLoadFile, true);
            vmLoadBW = new BufferedWriter(vmLoadFW);
            appendToFile(vmLoadBW, "#auto generated file!");

            genericFile = new File(outputFolder, filePrefix + "_GENERIC.log");
            genericFW = new FileWriter(genericFile, true);
            genericBW = new BufferedWriter(genericFW);
            appendToFile(genericBW, "#auto generated file!");

            taskDistFile = new File(outputFolder, filePrefix + "_TASK_DIST.log");
            taskDistFW = new FileWriter(taskDistFile, true);
            taskDistBW = new BufferedWriter(taskDistFW);
        }

        double totalTaskLength = 0;
        double totalTaskInputSize = 0;
        double totalTaskOutputSize = 0;
        int[] rsuAssignmentsAll = new int[SimManager.getInstance().getEdgeServerManager().getDatacenterList().size()];
        int[] rsuAssignmentsFail = new int[SimManager.getInstance().getEdgeServerManager().getDatacenterList().size()];

        int uncompletedTask = 0, completedTask = 0, failedTask = 0;
        double networkDelay = 0d, serviceTime = 0d, processingTime = 0d, cost = 0d;
        int failedTaskDueToVmCapacity = 0, failedTaskDuetoBw = 0, failedTaskDuetoMobility = 0, failedTaskDuetoNotInNetworkRange = 0;

        // extract the result of each task and write it to the file if required
        for (Map.Entry<Integer, LogItem> entry : taskMap.entrySet()) {
            Integer key = entry.getKey();
            LogItem value = entry.getValue();

            if (value.isInWarmUpPeriod())
                continue;

            //the status of the tasks are not important for this metric
            totalTaskLength += value.getTaskLenght();
            totalTaskInputSize += value.getTaskInputSize();
            totalTaskOutputSize += value.getTaskOutputSize();
            rsuAssignmentsAll[value.getHostId()]++;

            if (value.getStatus() == SimLogger.TASK_STATUS.COMLETED) {
                completedTask++;
                cost += value.getCost();
                serviceTime += value.getServiceTime();
                networkDelay += value.getNetworkDelay();
                processingTime += (value.getServiceTime() - value.getNetworkDelay());
                if (fileLogEnabled && SimSettings.getInstance().getDeepFileLoggingEnabled())
                    appendToFile(successBW, value.toString(key));
            } else if (value.getStatus() == SimLogger.TASK_STATUS.CREATED || value.getStatus() == SimLogger.TASK_STATUS.UPLOADING ||
                    value.getStatus() == SimLogger.TASK_STATUS.PROCESSING || value.getStatus() == SimLogger.TASK_STATUS.DOWNLOADING) {
                uncompletedTask++;
            } else {
                failedTask++;
                if (fileLogEnabled && SimSettings.getInstance().getDeepFileLoggingEnabled()) {
                    appendToFile(failBW, value.toString(key));
                }
                if (value.getStatus() == SimLogger.TASK_STATUS.REJECTED_DUE_TO_VM_CAPACITY) {
                    rsuAssignmentsFail[value.getHostId()]++;
                    failedTaskDueToVmCapacity++;
                } else if (value.getStatus() == SimLogger.TASK_STATUS.REJECTED_DUE_TO_BANDWIDTH
                        || value.getStatus() == SimLogger.TASK_STATUS.UNFINISHED_DUE_TO_BANDWIDTH) {
                    failedTaskDuetoBw++;
                } else if (value.getStatus() == SimLogger.TASK_STATUS.UNFINISHED_DUE_TO_MOBILITY) {
                    failedTaskDuetoMobility++;
                } else if (value.getStatus() == SimLogger.TASK_STATUS.REJECTED_DUE_TO_NOT_IN_NETWORK_RANGE) {
                    failedTaskDuetoNotInNetworkRange++;
                }
            }
        }

        // calculate server load
        double totalVmLoad = 0;
        for (VmLoadLogItem entry : vmLoadList) {
            totalVmLoad += entry.getAvgLoad();
            if (fileLogEnabled)
                appendToFile(vmLoadBW, entry.toString());
        }
        double avgServiceTime = 0d, avgProcessingTime = 0d, avgNetworkDelay = 0d, avgVmLoad = 0d, avgCost = 0d,
                avgTaskLength = 0d, avgTaskInputSize = 0d, avgTaskOutputSize = 0d;

        if (fileLogEnabled) {
            // write location logs to file
            //writeLocationsToFile(locationBW);

            // check  division by zero
            avgServiceTime = completedTask == 0 ? 0.0 : serviceTime / (double) completedTask;
            avgProcessingTime = completedTask == 0 ? 0.0 : processingTime / (double) completedTask;
            avgNetworkDelay = completedTask == 0 ? 0.0 : networkDelay / (double) completedTask;
            avgVmLoad = vmLoadList.size() == 0 ? 0.0 : totalVmLoad / (double) vmLoadList.size();
            avgCost = completedTask == 0 ? 0.0 : cost / (double) completedTask;
            avgTaskLength = totalTaskLength / taskMap.entrySet().size();
            avgTaskInputSize = totalTaskInputSize / taskMap.entrySet().size();
            avgTaskOutputSize = totalTaskOutputSize / taskMap.entrySet().size();


            // write generic results
            String genericResult = completedTask + SimSettings.DELIMITER
                    + failedTask + SimSettings.DELIMITER
                    + uncompletedTask + SimSettings.DELIMITER
                    + failedTaskDuetoBw + SimSettings.DELIMITER
                    + avgServiceTime + SimSettings.DELIMITER
                    + avgProcessingTime + SimSettings.DELIMITER
                    + avgVmLoad + SimSettings.DELIMITER
                    + avgNetworkDelay + SimSettings.DELIMITER
                    + avgCost + SimSettings.DELIMITER
                    + failedTaskDueToVmCapacity + SimSettings.DELIMITER
                    + failedTaskDuetoMobility + SimSettings.DELIMITER
                    + failedTaskDuetoNotInNetworkRange + SimSettings.DELIMITER
                    + avgTaskLength + SimSettings.DELIMITER
                    + avgTaskInputSize + SimSettings.DELIMITER
                    + avgTaskOutputSize;

            appendToFile(genericBW, genericResult);

            for (int i = 0; i < rsuAssignmentsAll.length; i++) {
                int noOfTasks = rsuAssignmentsAll[i];
                int noOfFailedTasks = rsuAssignmentsFail[i];
                double taskRateAll = ((double) noOfTasks / taskMap.entrySet().size()) * 100d;
                double taskRateFail = failedTaskDueToVmCapacity == 0 ? 0d : ((double) noOfFailedTasks / failedTaskDueToVmCapacity) * 100d;
                BigDecimal taskRateAllBD = new BigDecimal(Double.toString(taskRateAll));
                taskRateAllBD = taskRateAllBD.setScale(3, RoundingMode.HALF_UP);

                BigDecimal taskRateFailBD = new BigDecimal(Double.toString(taskRateFail));
                taskRateFailBD = taskRateFailBD.setScale(3, RoundingMode.HALF_UP);

                String distResult = i + SimSettings.DELIMITER
                        + noOfTasks + SimSettings.DELIMITER
                        + taskRateAllBD.doubleValue() + SimSettings.DELIMITER
                        + noOfFailedTasks + SimSettings.DELIMITER
                        + taskRateFailBD.doubleValue();

                appendToFile(taskDistBW, distResult);
            }

            // close open files
            if (SimSettings.getInstance().getDeepFileLoggingEnabled()) {
                successBW.close();
                failBW.close();
            }
            vmLoadBW.close();
            genericBW.close();
            taskDistBW.close();
        }

        // printout important results
        printLine("# of tasks: " + (failedTask + completedTask));
        printLine("# of skipped tasks(out of are range): " + numberOfSkippedTasks);
        printLine("# of failed tasks: " + failedTask);
        printLine("# of completed tasks: " + completedTask);
        printLine("# of uncompleted tasks : " + uncompletedTask);
        printLine("# of failed tasks due to vm capacity: " + failedTaskDueToVmCapacity);
        printLine("# of failed tasks due to Mobility/Bandwidth/Range: " + failedTaskDuetoMobility + "/" + failedTaskDuetoBw + "/" + failedTaskDuetoNotInNetworkRange);
        printLine("percentage of failed tasks: " + String.format("%.6f", ((double) failedTask * (double) 100) / (double) (completedTask + failedTask)) + "%");
        printLine("average service time: " + String.format("%.6f", avgServiceTime) + " seconds.");
        printLine("average processing time: " + String.format("%.6f", avgProcessingTime) + " seconds.");
        printLine("average network delay: " + String.format("%.6f", avgNetworkDelay) + " seconds.");
        printLine("average server utilization: " + String.format("%.6f", avgVmLoad));
        printLine("average task length: " + avgTaskLength);
        printLine("average task input size: " + avgTaskInputSize);
        printLine("average task output size: " + avgTaskOutputSize);

        // clear related collections (map list etc.)
        taskMap.clear();
        vmLoadList.clear();
    }

    private void writeLocationsToFile(BufferedWriter locationBW) throws IOException {
        TrafficLoadGenerator trafficLoadGenerator = (TrafficLoadGenerator) SimManager.getInstance().getLoadGeneratorModel();
        MobilityModel trafficMobility = SimManager.getInstance().getMobilityModel();

        for (int t = 0; t < (SimSettings.getInstance().getSimulationTime() / SimSettings.getInstance().getVmLocationLogInterval()); t++) {
            Double time = t * SimSettings.getInstance().getVmLocationLogInterval();
            if (time < SimSettings.getInstance().getWarmUpPeriod())
                continue;

            List<Integer> vehicleIdList = trafficLoadGenerator.getVehicleIdListByTime(time);
            for (Integer vehicleId : vehicleIdList) {
                Coordinate coordinate = (Coordinate) trafficMobility.getLocation(vehicleId, time);
                locationBW.write(time.toString() + SimSettings.DELIMITER + vehicleId + SimSettings.DELIMITER +
                        coordinate.getLat() + SimSettings.DELIMITER + coordinate.getLng());
                locationBW.newLine();
            }
        }
    }

    public int getNumberOfSkippedTasks() {
        return numberOfSkippedTasks;
    }

    public void setNumberOfSkippedTasks(int numberOfSkippedTasks) {
        this.numberOfSkippedTasks = numberOfSkippedTasks;
    }
}

class VmLoadLogItem {
    private double time;
    private double[] vmLoads;

    VmLoadLogItem(double _time, double[] vmLoads) {
        time = _time;
        this.vmLoads = vmLoads;
    }


    public double getAvgLoad() {
        return vmLoads[0];
    }


    public String toString() {
        String str = String.valueOf(time);
        for (double s : vmLoads) {
            str += SimSettings.DELIMITER + s;
        }
        return str;
    }
}

class LogItem {
    private SimLogger.TASK_STATUS status;
    private SimLogger.NETWORK_ERRORS networkError;
    private int datacenterId;
    private int hostId;
    private int vmId;
    private int vmType;
    private int taskType;
    private int taskLenght;
    private int taskInputSize;
    private int taskOutputSize;
    private double taskStartTime;
    private double taskEndTime;
    private double lanUploadDelay;
    private double manUploadDelay;
    private double wanUploadDelay;
    private double lanDownloadDelay;
    private double manDownloadDelay;
    private double wanDownloadDelay;
    private double bwCost;
    private double cpuCost;
    private boolean isInWarmUpPeriod;

    LogItem(int _taskType, int _taskLenght, int _taskInputType, int _taskOutputSize) {
        taskType = _taskType;
        taskLenght = _taskLenght;
        taskInputSize = _taskInputType;
        taskOutputSize = _taskOutputSize;
        networkError = NETWORK_ERRORS.NONE;
        status = SimLogger.TASK_STATUS.CREATED;
        taskEndTime = 0;
    }

    public void taskStarted(double time) {
        taskStartTime = time;
        status = SimLogger.TASK_STATUS.UPLOADING;

        if (time < SimSettings.getInstance().getWarmUpPeriod())
            isInWarmUpPeriod = true;
        else
            isInWarmUpPeriod = false;
    }

    public void setUploadDelay(double delay, NETWORK_DELAY_TYPES delayType) {
        if (delayType == NETWORK_DELAY_TYPES.WLAN_DELAY)
            lanUploadDelay = delay;
        else if (delayType == NETWORK_DELAY_TYPES.MAN_DELAY)
            manUploadDelay = delay;
        else if (delayType == NETWORK_DELAY_TYPES.WAN_DELAY)
            wanUploadDelay = delay;
    }

    public void setDownloadDelay(double delay, NETWORK_DELAY_TYPES delayType) {
        if (delayType == NETWORK_DELAY_TYPES.WLAN_DELAY)
            lanDownloadDelay = delay;
        else if (delayType == NETWORK_DELAY_TYPES.MAN_DELAY)
            manDownloadDelay = delay;
        else if (delayType == NETWORK_DELAY_TYPES.WAN_DELAY)
            wanDownloadDelay = delay;
    }

    public void taskAssigned(int _datacenterId, int _hostId, int _vmId, int _vmType) {
        status = SimLogger.TASK_STATUS.PROCESSING;
        datacenterId = _datacenterId;
        hostId = _hostId;
        vmId = _vmId;
        vmType = _vmType;
    }

    public void taskExecuted() {
        status = SimLogger.TASK_STATUS.DOWNLOADING;
    }

    public void taskEnded(double time) {
        taskEndTime = time;
        status = SimLogger.TASK_STATUS.COMLETED;
    }

    public void taskRejectedDueToVMCapacity(double time, int _vmType) {
        vmType = _vmType;
        taskEndTime = time;
        status = SimLogger.TASK_STATUS.REJECTED_DUE_TO_VM_CAPACITY;
    }

    public void taskRejectedDueToVMCapacity(double time, int _hostId, int _vmType) {
        vmType = _vmType;
        hostId = _hostId;
        taskEndTime = time;
        status = SimLogger.TASK_STATUS.REJECTED_DUE_TO_VM_CAPACITY;
    }

    public void rejectedDueToNotInNetworkRange(double time) {
        taskEndTime = time;
        status = SimLogger.TASK_STATUS.REJECTED_DUE_TO_NOT_IN_NETWORK_RANGE;
    }

    public void taskRejectedDueToBandwidth(double time, int _vmType, NETWORK_DELAY_TYPES delayType) {
        vmType = _vmType;
        taskEndTime = time;
        status = SimLogger.TASK_STATUS.REJECTED_DUE_TO_BANDWIDTH;

        if (delayType == NETWORK_DELAY_TYPES.WLAN_DELAY)
            networkError = NETWORK_ERRORS.LAN_ERROR;
        else if (delayType == NETWORK_DELAY_TYPES.MAN_DELAY)
            networkError = NETWORK_ERRORS.MAN_ERROR;
        else if (delayType == NETWORK_DELAY_TYPES.WAN_DELAY)
            networkError = NETWORK_ERRORS.WAN_ERROR;
    }

    public void taskFailedDueToBandwidth(double time, NETWORK_DELAY_TYPES delayType) {
        taskEndTime = time;
        status = SimLogger.TASK_STATUS.UNFINISHED_DUE_TO_BANDWIDTH;

        if (delayType == NETWORK_DELAY_TYPES.WLAN_DELAY)
            networkError = NETWORK_ERRORS.LAN_ERROR;
        else if (delayType == NETWORK_DELAY_TYPES.MAN_DELAY)
            networkError = NETWORK_ERRORS.MAN_ERROR;
        else if (delayType == NETWORK_DELAY_TYPES.WAN_DELAY)
            networkError = NETWORK_ERRORS.WAN_ERROR;
    }

    public void taskFailedDueToMobility(double time) {
        taskEndTime = time;
        status = SimLogger.TASK_STATUS.UNFINISHED_DUE_TO_MOBILITY;
    }

    public void setCost(double _bwCost, double _cpuCos) {
        bwCost = _bwCost;
        cpuCost = _cpuCos;
    }

    public boolean isInWarmUpPeriod() {
        return isInWarmUpPeriod;
    }

    public double getCost() {
        return bwCost + cpuCost;
    }

    public double getNetworkUploadDelay(NETWORK_DELAY_TYPES delayType) {
        double result = 0;
        if (delayType == NETWORK_DELAY_TYPES.WLAN_DELAY)
            result = lanUploadDelay;
        else if (delayType == NETWORK_DELAY_TYPES.MAN_DELAY)
            result = manUploadDelay;
        else if (delayType == NETWORK_DELAY_TYPES.WAN_DELAY)
            result = wanUploadDelay;

        return result;
    }

    public double getNetworkDownloadDelay(NETWORK_DELAY_TYPES delayType) {
        double result = 0;
        if (delayType == NETWORK_DELAY_TYPES.WLAN_DELAY)
            result = lanDownloadDelay;
        else if (delayType == NETWORK_DELAY_TYPES.MAN_DELAY)
            result = manDownloadDelay;
        else if (delayType == NETWORK_DELAY_TYPES.WAN_DELAY)
            result = wanDownloadDelay;

        return result;
    }

    public double getNetworkDelay(NETWORK_DELAY_TYPES delayType) {
        double result = 0;
        if (delayType == NETWORK_DELAY_TYPES.WLAN_DELAY)
            result = lanDownloadDelay + lanUploadDelay;
        else if (delayType == NETWORK_DELAY_TYPES.MAN_DELAY)
            result = manDownloadDelay + manUploadDelay;
        else if (delayType == NETWORK_DELAY_TYPES.WAN_DELAY)
            result = wanDownloadDelay + wanUploadDelay;

        return result;
    }

    public double getNetworkDelay() {
        return lanUploadDelay +
                manUploadDelay +
                wanUploadDelay +
                lanDownloadDelay +
                manDownloadDelay +
                wanDownloadDelay;
    }

    public double getServiceTime() {
        return taskEndTime - taskStartTime;
    }

    public SimLogger.TASK_STATUS getStatus() {
        return status;
    }

    public SimLogger.NETWORK_ERRORS getNetworkError() {
        return networkError;
    }

    public int getVmType() {
        return vmType;
    }

    public int getTaskType() {
        return taskType;
    }

    public String toString(int taskId) {
        String result = taskId + SimSettings.DELIMITER + datacenterId + SimSettings.DELIMITER + hostId
                + SimSettings.DELIMITER + vmId + SimSettings.DELIMITER + vmType + SimSettings.DELIMITER + taskType
                + SimSettings.DELIMITER + taskLenght + SimSettings.DELIMITER + taskInputSize + SimSettings.DELIMITER
                + taskOutputSize + SimSettings.DELIMITER + taskStartTime + SimSettings.DELIMITER + taskEndTime
                + SimSettings.DELIMITER;

        if (status == SimLogger.TASK_STATUS.COMLETED) {
            result += getNetworkDelay() + SimSettings.DELIMITER;
            result += getNetworkDelay(NETWORK_DELAY_TYPES.WLAN_DELAY) + SimSettings.DELIMITER;
            result += getNetworkDelay(NETWORK_DELAY_TYPES.MAN_DELAY) + SimSettings.DELIMITER;
            result += getNetworkDelay(NETWORK_DELAY_TYPES.WAN_DELAY);
        } else if (status == SimLogger.TASK_STATUS.REJECTED_DUE_TO_VM_CAPACITY)
            result += "1"; // failure reason 1
        else if (status == SimLogger.TASK_STATUS.REJECTED_DUE_TO_BANDWIDTH)
            result += "2"; // failure reason 2
        else if (status == SimLogger.TASK_STATUS.UNFINISHED_DUE_TO_BANDWIDTH)
            result += "3"; // failure reason 3
        else if (status == SimLogger.TASK_STATUS.UNFINISHED_DUE_TO_MOBILITY)
            result += "4"; // failure reason 4
        else if (status == SimLogger.TASK_STATUS.REJECTED_DUE_TO_NOT_IN_NETWORK_RANGE)
            result += "5"; // failure reason 5
        else
            result += "0"; // default failure reason
        return result;
    }

    public int getTaskLenght() {
        return taskLenght;
    }

    public int getTaskInputSize() {
        return taskInputSize;
    }

    public int getTaskOutputSize() {
        return taskOutputSize;
    }

    public int getHostId() {
        return hostId;
    }
}
