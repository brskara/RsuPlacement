/*
 * Title:        EdgeCloudSim - Mobile Server Manager
 * 
 * Description: 
 * DefaultMobileServerManager is responsible for creating datacenters, hosts and VMs.
 *
 * Please note that the mobile processing units are simulated via
 * CloudSim. It is assumed that the mobile devices operate Hosts
 * and VMs like a server. That is why the class names are similar
 * to other Cloud and Edge components (to provide consistency).
 *
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 * Copyright (c) 2017, Bogazici University, Istanbul, Turkey
 */

package edu.boun.edgecloudsim.applications.rsu_placement;

import edu.boun.edgecloudsim.cloud_server.CloudServerManager;
import edu.boun.edgecloudsim.edge_client.mobile_processing_unit.MobileServerManager;
import edu.boun.edgecloudsim.edge_client.mobile_processing_unit.MobileVmAllocationPolicy_Custom;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.VmAllocationPolicy;

import java.util.List;

public class ImpotentCloudServerManager extends CloudServerManager {

	public ImpotentCloudServerManager() {

	}

	@Override
	public void initialize() {
	}
	
	@Override
	public VmAllocationPolicy getVmAllocationPolicy(List<? extends Host> list, int dataCenterIndex) {
		return new MobileVmAllocationPolicy_Custom(list, dataCenterIndex);
	}

	@Override
	public void startDatacenters() throws Exception {
		//local computation is not supported in default Mobile Device Manager
	}

	@Override
	public void terminateDatacenters() {
		//local computation is not supported in default Mobile Device Manager
	}

	@Override
	public void createVmList(int brockerId) {
		//local computation is not supported in default Mobile Device Manager
	}

	@Override
	public double getAvgUtilization() {
		//local computation is not supported in default Mobile Device Manager
		return 0;
	}
}
