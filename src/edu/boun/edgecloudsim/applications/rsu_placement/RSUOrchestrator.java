package edu.boun.edgecloudsim.applications.rsu_placement;

import edu.boun.edgecloudsim.applications.rsu_placement.model.Coordinate;
import edu.boun.edgecloudsim.applications.rsu_placement.model.RSU;
import edu.boun.edgecloudsim.applications.rsu_placement.utils.CoordinateUtils;
import edu.boun.edgecloudsim.core.SimManager;
import edu.boun.edgecloudsim.core.SimSettings;
import edu.boun.edgecloudsim.edge_client.Task;
import edu.boun.edgecloudsim.edge_orchestrator.EdgeOrchestrator;
import edu.boun.edgecloudsim.edge_server.EdgeVM;
import edu.boun.edgecloudsim.utils.SimLogger;
import edu.wlu.cs.levy.CG.KDTree;
import edu.wlu.cs.levy.CG.KeyDuplicateException;
import edu.wlu.cs.levy.CG.KeySizeException;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.core.SimEvent;


public class RSUOrchestrator extends EdgeOrchestrator {

    private KDTree<RSU> rsuRadar = new KDTree<RSU>(2);

    public RSUOrchestrator() {
        super();
    }

    @Override
    public void initialize() {
    }

    @Override
    public int getDeviceToOffload(Task task) {
        return SimSettings.GENERIC_EDGE_DEVICE_ID;
    }

    @Override
    public EdgeVM getVmToOffload(Task task, int deviceId) {
        //TODO multiple task assignments at each timestep causes capacity overload, is it ok?
        RSU rsu = findRSUInRangeByCoordinate(task.getCoordinate());
        if(rsu == null){
            return null;
        }
        return SimManager.getInstance().getEdgeServerManager().getVmList(rsu.getId()).get(0);
    }

    public RSU findRSUInRangeByCoordinate(Coordinate coordinate){
        double[] position = {CoordinateUtils.lon2x(coordinate.getLng()), CoordinateUtils.lat2y(coordinate.getLat())};
        RSU rsu = null;
        try {
            rsu = rsuRadar.nearest(position);
        } catch (Exception e) {
            SimLogger.printLine("Problem encountered while finding nearest RSU");
            System.exit(0);
        }
        //car should be in the range of nearest RSU
        double distance = CoordinateUtils.calculateDistance(coordinate, rsu.getCoordinate());
        if(distance > SimSettings.getInstance().getWlanRange()){
            return null;
        }
        return rsu;
    }

    @Override
    public void startEntity() {
        for(Datacenter dc : SimManager.getInstance().getEdgeServerManager().getDatacenterList()){
            RSU rsu = (RSU)dc.getHostList().get(0);
            double[] rsuPosition = {CoordinateUtils.lon2x(rsu.getCoordinate().getLng()), CoordinateUtils.lat2y(rsu.getCoordinate().getLat())};
            try {
                rsuRadar.insert(rsuPosition, rsu);
            } catch (KeySizeException |  KeyDuplicateException e) {
                SimLogger.printLine("Problem encountered while saving RSU positions");
                System.exit(0);
            }
        }
    }

    @Override
    public void processEvent(SimEvent simEvent) {
        // TODO Auto-generated method stub
    }

    @Override
    public void shutdownEntity() {
        // TODO Auto-generated method stub
    }
}
