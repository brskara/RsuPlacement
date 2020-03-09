/*
 * Title:        EdgeCloudSim - M/M/1 Queue model implementation
 * 
 * Description: 
 * MM1Queue implements M/M/1 Queue model for WLAN and WAN communication
 * 
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 * Copyright (c) 2017, Bogazici University, Istanbul, Turkey
 */

package edu.boun.edgecloudsim.applications.rsu_placement;

import edu.boun.edgecloudsim.applications.rsu_placement.model.Coordinate;
import edu.boun.edgecloudsim.applications.rsu_placement.model.RSU;
import edu.boun.edgecloudsim.core.SimManager;
import edu.boun.edgecloudsim.core.SimSettings;
import edu.boun.edgecloudsim.edge_client.Task;
import edu.boun.edgecloudsim.edge_server.EdgeHost;
import edu.boun.edgecloudsim.mobility.MobilityModel;
import edu.boun.edgecloudsim.network.NetworkModel;
import edu.boun.edgecloudsim.utils.Location;
import org.cloudbus.cloudsim.core.CloudSim;

import java.util.List;
import java.util.Objects;

public class RSUMM1Queue extends NetworkModel {
	private double wLanPoissonMean; //seconds
	private double avgTaskInputSize; //bytes
	private double avgTaskOutputSize; //bytes

	public RSUMM1Queue(int _numberOfMobileDevices) {
		super(_numberOfMobileDevices, "");
	}


	@Override
	public void initialize() {
		//get interarrival time and task sizes
		SimSettings SS = SimSettings.getInstance();
		wLanPoissonMean = SS.getTaskLookUpTable()[0][2];
		avgTaskInputSize = SS.getTaskLookUpTable()[0][5];
		avgTaskOutputSize = SS.getTaskLookUpTable()[0][6];
	}

    //dest device is always edge device in our simulation scenarios!
	@Override
	public double getUploadDelay(int vehicleId, int destDeviceId, Task task) {
		//vehicle to edge device (wifi access point)
		return calculateMM1(0,
				SimSettings.getInstance().getWlanBandwidth(),
				wLanPoissonMean,
				avgTaskInputSize,
				getVehicleCountInTheSameRSURange(task));
	}


    //source device is always edge device in our simulation scenarios!
	@Override
	public double getDownloadDelay(int sourceDeviceId, int vehicleId, Task task) {
		//edge device (wifi access point) to mobile device
		return calculateMM1(0,
				SimSettings.getInstance().getWlanBandwidth(),
				wLanPoissonMean,
				avgTaskOutputSize,
				getVehicleCountInTheSameRSURange(task));
	}

	private long getVehicleCountInTheSameRSURange(Task task){
		TrafficLoadGenerator trafficLoadGenerator = (TrafficLoadGenerator) SimManager.getInstance().getLoadGeneratorModel();
		RSUOrchestrator rsuOrchestrator = (RSUOrchestrator) SimManager.getInstance().getEdgeOrchestrator();
		MobilityModel trafficMobility = SimManager.getInstance().getMobilityModel();
		Integer rsuId;
		if(task.getVmId() >= 0){
			//for download delay, vm is already assigned
			rsuId = task.getVmId();
		}else{
			//for upload delay, vm is not assigned
			rsuId = rsuOrchestrator.findRSUInRangeByCoordinate(task.getCoordinate()).getId();
		}
		double time = CloudSim.clock();
		List<Integer> vehicleIdList = trafficLoadGenerator.getVehicleIdListByTime(time);
		return vehicleIdList.stream()
				.map(e -> (Coordinate)trafficMobility.getLocation(e, time))
				.map(rsuOrchestrator::findRSUInRangeByCoordinate)
				.filter(Objects::nonNull)
				.map(RSU::getId)
				.filter(rsuId::equals)
				.count();
	}

	private double calculateMM1(double propogationDelay, int bandwidth /*Kbps*/, double poissonMean, double avgTaskSize /*KB*/, long deviceCount){
		double bps, mu, lamda, totalLamda;
		//avgTaskSize = avgTaskSize * 1000d; //data defined in bytes
		bps = (bandwidth * 1000d) / 8d; //convert from Kbps to Byte per seconds
		lamda = 1d/poissonMean; //task arrival rate for 1 vehicle
		totalLamda = lamda*(double)deviceCount; //task arrival rate for all vehicles in the range
		mu = bps / avgTaskSize ; //service rate
		if(totalLamda == mu){ //in mm1 queue model, mu has to be bigger than totalLamda, in case it is equal or less, task will be rejected
			return 0;
		}
		double result = 1d / (mu-lamda*(double)deviceCount); //average waiting time in mm1 queue
		result += propogationDelay;
		return (result > 5) ? -1 : result;
	}


	@Override
	public void uploadStarted(Location accessPointLocation, int destDeviceId) {
		// Auto-generated method stub

	}

	@Override
	public void uploadFinished(Location accessPointLocation, int destDeviceId) {
		// Auto-generated method stub

	}

	@Override
	public void downloadStarted(Location accessPointLocation, int sourceDeviceId) {
		// Auto-generated method stub
		
	}

	@Override
	public void downloadFinished(Location accessPointLocation, int sourceDeviceId) {
		// Auto-generated method stub
		
	}
}
