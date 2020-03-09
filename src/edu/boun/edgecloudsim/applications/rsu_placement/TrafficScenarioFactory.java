/*
 * Title:        EdgeCloudSim - Scenario Factory
 *
 * Description:  Sample scenario factory providing the default
 *               instances of required abstract classes
 *
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 * Copyright (c) 2017, Bogazici University, Istanbul, Turkey
 */

package edu.boun.edgecloudsim.applications.rsu_placement;

import edu.boun.edgecloudsim.cloud_server.CloudServerManager;
import edu.boun.edgecloudsim.core.ScenarioFactory;
import edu.boun.edgecloudsim.edge_client.MobileDeviceManager;
import edu.boun.edgecloudsim.edge_client.mobile_processing_unit.MobileServerManager;
import edu.boun.edgecloudsim.edge_orchestrator.EdgeOrchestrator;
import edu.boun.edgecloudsim.edge_server.EdgeServerManager;
import edu.boun.edgecloudsim.mobility.MobilityModel;
import edu.boun.edgecloudsim.network.MM1Queue;
import edu.boun.edgecloudsim.network.NetworkModel;
import edu.boun.edgecloudsim.task_generator.LoadGeneratorModel;

public class TrafficScenarioFactory implements ScenarioFactory {
    private int numOfMobileDevice;
    private String trafficScenarioFile;

    TrafficScenarioFactory(int numOfMobileDevice, String trafficScenarioFile) {
        this.numOfMobileDevice = numOfMobileDevice;
        this.trafficScenarioFile = trafficScenarioFile;
    }

    @Override
    public LoadGeneratorModel getLoadGeneratorModel() {
        return new TrafficLoadGenerator(trafficScenarioFile);
    }

    @Override
    public MobilityModel getMobilityModel() {
        return new VehicleMobility(trafficScenarioFile);
    }

    @Override
    public EdgeOrchestrator getEdgeOrchestrator() {
        return new RSUOrchestrator();
    }

    @Override
    public EdgeServerManager getEdgeServerManager() {
        return new RSUManager();
    }

    @Override
    public MobileDeviceManager getMobileDeviceManager() throws Exception {
        return new TrafficTaskBroker();
    }

    @Override
    public NetworkModel getNetworkModel() {
        return new RSUMM1Queue(numOfMobileDevice);
    }

    @Override
    public CloudServerManager getCloudServerManager() {
        //there is no cloud computing in this scenario
        return new ImpotentCloudServerManager();
    }

    @Override
    public MobileServerManager getMobileServerManager() {
        //there is no mobile computing in this scenario
        return new ImpotentMobileServerManager();
    }
}
