/*
 * Title:        EdgeCloudSim - Mobile Device Manager
 * 
 * Description: 
 * DefaultMobileDeviceManager is responsible for submitting the tasks to the related
 * device by using the Edge Orchestrator. It also takes proper actions 
 * when the execution of the tasks are finished.
 * By default, DefaultMobileDeviceManager sends tasks to the edge servers or
 * cloud servers. If you want to use different topology, for example
 * MAN edge server, you should modify the flow defined in this class.
 * 
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 * Copyright (c) 2017, Bogazici University, Istanbul, Turkey
 */

package edu.boun.edgecloudsim.applications.rsu_placement;

import edu.boun.edgecloudsim.applications.rsu_placement.model.Coordinate;
import edu.boun.edgecloudsim.applications.rsu_placement.model.RSU;
import edu.boun.edgecloudsim.core.SimManager;
import edu.boun.edgecloudsim.core.SimSettings;
import edu.boun.edgecloudsim.core.SimSettings.NETWORK_DELAY_TYPES;
import edu.boun.edgecloudsim.edge_client.CpuUtilizationModel_Custom;
import edu.boun.edgecloudsim.edge_client.MobileDeviceManager;
import edu.boun.edgecloudsim.edge_client.Task;
import edu.boun.edgecloudsim.edge_server.EdgeVM;
import edu.boun.edgecloudsim.mobility.MobilityModel;
import edu.boun.edgecloudsim.network.NetworkModel;
import edu.boun.edgecloudsim.utils.SimLogger;
import edu.boun.edgecloudsim.utils.TaskProperty;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;

public class TrafficTaskBroker extends MobileDeviceManager {
	private static final int BASE = 100000; //start from base in order not to conflict cloudsim tag!
	private static final int REQUEST_RECEIVED_BY_CLOUD = BASE + 1;
	private static final int REQUEST_RECIVED_BY_EDGE_DEVICE = BASE + 2;
	private static final int RESPONSE_RECEIVED_BY_MOBILE_DEVICE = BASE + 3;
	private int taskIdCounter=0;
	private long[] capacity = new long[(int) SimSettings.getInstance().getSimulationTime()];

	public TrafficTaskBroker() throws Exception{
	}

	@Override
	public void initialize() {

	}

	public void submitTask(TaskProperty edgeTask) {
		NetworkModel networkModel = SimManager.getInstance().getNetworkModel();

		Task task = createTask(edgeTask);
		Coordinate coordinate = (Coordinate) SimManager.getInstance().getMobilityModel().getLocation(task.getMobileDeviceId(),CloudSim.clock());
		//set location of the mobile device which generates this task
		task.setCoordinate(coordinate);

		//add related task to log list
		SimLogger.getInstance().addLog(task.getCloudletId(),
				task.getTaskType(),
				(int)task.getCloudletLength(),
				(int)task.getCloudletFileSize(),
				(int)task.getCloudletOutputSize());

		RSUOrchestrator rsuOrchestrator = (RSUOrchestrator) SimManager.getInstance().getEdgeOrchestrator();
		RSU rsu = rsuOrchestrator.findRSUInRangeByCoordinate(coordinate);
		if(rsu == null){
			//vehicle is not in RSU range
			SimLogger.getInstance().rejectedDueToNotInNetworkRange(task.getCloudletId(), CloudSim.clock());
			return;
		}
		double wLanDelay = networkModel.getUploadDelay(task.getMobileDeviceId(), SimSettings.GENERIC_EDGE_DEVICE_ID, task);
		if(wLanDelay > 0){
			schedule(getId(), wLanDelay, REQUEST_RECIVED_BY_EDGE_DEVICE, task);
			SimLogger.getInstance().taskStarted(task.getCloudletId(), CloudSim.clock());
			SimLogger.getInstance().setUploadDelay(task.getCloudletId(), wLanDelay, NETWORK_DELAY_TYPES.WLAN_DELAY);
		}
		else {
			SimLogger.getInstance().rejectedDueToBandwidth(
					task.getCloudletId(),
					CloudSim.clock(),
					SimSettings.VM_TYPES.EDGE_VM.ordinal(),
					NETWORK_DELAY_TYPES.WLAN_DELAY);
		}

	}

	protected void processCloudletReturn(SimEvent ev) {
		NetworkModel networkModel = SimManager.getInstance().getNetworkModel();
		Task task = (Task) ev.getData();
		SimLogger.getInstance().taskExecuted(task.getCloudletId());

		double wLanDelay = networkModel.getDownloadDelay(task.getAssociatedHostId(), task.getMobileDeviceId(), task);
		if(wLanDelay > 0) {
			MobilityModel trafficMobility = SimManager.getInstance().getMobilityModel();
			RSUOrchestrator rsuOrchestrator = (RSUOrchestrator) SimManager.getInstance().getEdgeOrchestrator();
			Coordinate finalCoordinate = (Coordinate) trafficMobility.getLocation(task.getMobileDeviceId(),CloudSim.clock() + wLanDelay);
			RSU finalRSUVehicleInTheRangeOf = rsuOrchestrator.findRSUInRangeByCoordinate(finalCoordinate);
			//if the vehicle is out of the RSU range, task is failed
			//TODO validate vmid
			if(finalRSUVehicleInTheRangeOf != null && task.getVmId() == finalRSUVehicleInTheRangeOf.getId()) {
				networkModel.downloadStarted(finalCoordinate, SimSettings.GENERIC_EDGE_DEVICE_ID);
				SimLogger.getInstance().setDownloadDelay(task.getCloudletId(), wLanDelay, NETWORK_DELAY_TYPES.WLAN_DELAY);
				schedule(getId(), wLanDelay, RESPONSE_RECEIVED_BY_MOBILE_DEVICE, task);
			}
			else {
				SimLogger.getInstance().failedDueToMobility(task.getCloudletId(), CloudSim.clock());
			}
		}
		else {
			SimLogger.getInstance().failedDueToBandwidth(task.getCloudletId(), CloudSim.clock(), NETWORK_DELAY_TYPES.WLAN_DELAY);
		}

	}

	protected void processOtherEvent(SimEvent ev) {
		if (ev == null) {
			SimLogger.printLine(getName() + ".processOtherEvent(): " + "Error - an event is null! Terminating simulation...");
			System.exit(0);
			return;
		}
		Task task = (Task) ev.getData();
		switch (ev.getTag()) {
			case REQUEST_RECIVED_BY_EDGE_DEVICE: {
				submitTaskToVm(task, 0, SimSettings.GENERIC_EDGE_DEVICE_ID);
				break;
			}
			case RESPONSE_RECEIVED_BY_MOBILE_DEVICE: {
				SimLogger.getInstance().taskEnded(task.getCloudletId(), CloudSim.clock());
				break;
			}
			default:
				SimLogger.printLine(getName() + ".processOtherEvent(): " + "Error - event unknown by this DatacenterBroker. Terminating simulation...");
				System.exit(0);
				break;
		}
	}

	@Override
	public UtilizationModel getCpuUtilizationModel() {
		return new CpuUtilizationModel_Custom();
	}

	protected void submitCloudlets() {
		//do nothing!
	}

	private void submitTaskToVm(Task task, double delay, int datacenterId) {
		//select a VM
		EdgeVM selectedVM = (EdgeVM)SimManager.getInstance().getEdgeOrchestrator().getVmToOffload(task, datacenterId);
		int vmType = SimSettings.VM_TYPES.EDGE_VM.ordinal();

		if(selectedVM != null){
			if(!hasVMEnoughCapacity(task, selectedVM)){
				SimLogger.getInstance().rejectedDueToVMCapacity(task.getCloudletId(), selectedVM.getHost().getId(), CloudSim.clock(), vmType);
				return;
			}

			task.setAssociatedDatacenterId(selectedVM.getHost().getDatacenter().getId());

			//save related host id
			task.setAssociatedHostId(selectedVM.getHost().getId());
			
			//set related vm id
			task.setAssociatedVmId(selectedVM.getId());
			
			//bind task to related VM
			getCloudletList().add(task);
			bindCloudletToVm(task.getCloudletId(),selectedVM.getId());
			
			schedule(getVmsToDatacentersMap().get(task.getVmId()), delay, CloudSimTags.CLOUDLET_SUBMIT, task);


			SimLogger.getInstance().taskAssigned(task.getCloudletId(),
					selectedVM.getHost().getDatacenter().getId(),
					selectedVM.getHost().getId(),
					selectedVM.getId(),
					vmType);
		}
		else{
			SimLogger.printLine(getName() + ".submitTaskToVm(): " + "Error - vehicle which is not in an RSU range cannot create task");
			System.exit(0);
		}
	}

	private boolean hasVMEnoughCapacity(Task task, EdgeVM selectedVM) {
		double requiredCapacity = ((CpuUtilizationModel_Custom)task.getUtilizationModelCpu()).predictUtilization(selectedVM.getVmType());
		double targetVmCapacity = (double)100 - selectedVM.getCloudletScheduler().getTotalUtilizationOfCpu(CloudSim.clock());
		return targetVmCapacity >= requiredCapacity;
	}

	private Task createTask(TaskProperty edgeTask){
		UtilizationModel utilizationModel = new UtilizationModelFull(); /*UtilizationModelStochastic*/
		UtilizationModel utilizationModelCPU = getCpuUtilizationModel();

		Task task = new Task(edgeTask.getMobileDeviceId(), ++taskIdCounter,
				edgeTask.getLength(), edgeTask.getPesNumber(),
				edgeTask.getInputFileSize(), edgeTask.getOutputFileSize(),
				utilizationModelCPU, utilizationModel, utilizationModel);

		//set the owner of this task
		task.setUserId(this.getId());
		task.setTaskType(edgeTask.getTaskType());

		if (utilizationModelCPU instanceof CpuUtilizationModel_Custom) {
			((CpuUtilizationModel_Custom)utilizationModelCPU).setTask(task);
		}

		return task;
	}
}
