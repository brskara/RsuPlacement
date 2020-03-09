/*
 * Title:        EdgeCloudSim - Main Application
 *
 * Description:  Main application for Simple App
 *
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 * Copyright (c) 2017, Bogazici University, Istanbul, Turkey
 */

package edu.boun.edgecloudsim.applications.rsu_placement;

import edu.boun.edgecloudsim.core.ScenarioFactory;
import edu.boun.edgecloudsim.core.SimManager;
import edu.boun.edgecloudsim.core.SimSettings;
import edu.boun.edgecloudsim.utils.SimLogger;
import edu.boun.edgecloudsim.utils.SimUtils;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainApp {

    /**
     * Creates main() to run this example
     */
    public static void main(String[] args) {
        //disable console output of cloudsim library
        Log.disable();

//        if (args.length != 6) {
//            throw new IllegalArgumentException();
//        }

        //enable console ourput and file output of this application
        SimLogger.enablePrintLog();

        String configFile, edgeDevicesFile, applicationsFile, outputFolder, inputFile;
        int numberOfVehicles;

        if (args.length == 6) {
            configFile = args[0];
            edgeDevicesFile = args[1];
            applicationsFile = args[2];
            outputFolder = args[3];
            inputFile = args[4];
            numberOfVehicles = Integer.parseInt(args[5]);
        }else {
            SimLogger.printLine("Simulation setting file, output folder and iteration number are not provided! Using default ones...");
            configFile = "scripts/rsu_placement/config/default_config.properties";
            edgeDevicesFile = "scripts/rsu_placement/config/single_rsu.xml";
            applicationsFile = "scripts/rsu_placement/config/applications.xml";
            outputFolder = "scripts/rsu_placement/output-app/";
            numberOfVehicles = 500;
            //inputFile = "scripts/rsu_placement/input/traffic"+numberOfVehicles+".xml";
            inputFile = "scripts/rsu_placement/input/traffic20.xml";
        }

        //load settings from configuration file
        SimSettings SS = SimSettings.getInstance();
        if (SS.initialize(configFile, edgeDevicesFile, applicationsFile) == false) {
            SimLogger.printLine("cannot initialize simulation settings!");
            System.exit(0);
        }

        if (SS.getFileLoggingEnabled()) {
            SimLogger.enableFileLog();
            SimUtils.cleanOutputFolder(outputFolder);
        }

        DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date SimulationStartDate = Calendar.getInstance().getTime();
        String now = df.format(SimulationStartDate);
        SimLogger.printLine("Simulation started at " + now);
        SimLogger.printLine("----------------------------------------------------------------------");

        Date ScenarioStartDate = Calendar.getInstance().getTime();
        now = df.format(ScenarioStartDate);

        SimLogger.printLine("Scenario started at " + now);
        SimLogger.printLine("Duration: " + SS.getSimulationTime()/60 + " min (warm up period: "+ SS.getWarmUpPeriod() +" sec) - #vehicles: " + numberOfVehicles);
        SimLogger.getInstance().simStarted(outputFolder, "SIMRESULT_" + numberOfVehicles + "VEHICLES");

        try {
            // First step: Initialize the CloudSim package. It should be called
            // before creating any entities.
            int num_user = 2;   // number of grid users
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false;  // mean trace events

            // Initialize the CloudSim library
            CloudSim.init(num_user, calendar, trace_flag, 0.01);

            // Generate EdgeCloudsim Scenario Factory
            //String inputFile = inputFolder + "/traffic" + i + ".xml";
            ScenarioFactory sampleFactory = new TrafficScenarioFactory(numberOfVehicles, inputFile);

            // Generate EdgeCloudSim Simulation Manager
            SimManager manager = new SimManager(sampleFactory, numberOfVehicles, "", "");

            // Start simulation
            manager.startSimulation();
        } catch (Exception e) {
            SimLogger.printLine("The simulation has been terminated due to an unexpected error");
            e.printStackTrace();
            System.exit(0);
        }

        Date ScenarioEndDate = Calendar.getInstance().getTime();
        now = df.format(ScenarioEndDate);
        SimLogger.printLine("Scenario finished at " + now + ". It took " + SimUtils.getTimeDifference(ScenarioStartDate, ScenarioEndDate));
        SimLogger.printLine("----------------------------------------------------------------------");

        Date SimulationEndDate = Calendar.getInstance().getTime();
        now = df.format(SimulationEndDate);
        SimLogger.printLine("Simulation finished at " + now + ". It took " + SimUtils.getTimeDifference(SimulationStartDate, SimulationEndDate));
    }
}
