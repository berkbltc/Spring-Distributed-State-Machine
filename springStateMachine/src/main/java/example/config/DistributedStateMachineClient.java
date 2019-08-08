package example.config;

import example.entity.TransitionContext;
import example.runner.MachineRunner;
import example.util.SpringUtils;
import hm.curatorlib.cluster.ClusterMembers;
import hm.curatorlib.core.ClientConnector;
import hm.curatorlib.core.LeaderJob;
import hm.curatorlib.core.PeerStateChanges;
import hm.curatorlib.leader.LeaderElector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service("distributeService")
public class DistributedStateMachineClient implements PeerStateChanges, LeaderJob {

    Logger logger = LoggerFactory.getLogger(DistributedStateMachineClient.class);

    private LeaderElector le = null;
    private ClusterMembers cm = null;
    private ClientConnector cn = null;
    private ArrayList<String> peersUp;
    private ArrayList<String> peersDown;
    private List<TransitionContext> contextList;

    public DistributedStateMachineClient() {
    }

    public DistributedStateMachineClient(String connectionString, String clientName, String groupName,
                                         String groupPath, String leaderSelectionPath, int sleepMsBetweenRetries,
                                         int maxRetries, List<String> clusterMembers) {

        peersDown = new ArrayList<>();
        peersUp = new ArrayList<>();

        // first of all we have to create a client to connect to ZK cluster via curator
        this.cn = new ClientConnector(connectionString,
                clientName,
                groupName,
                leaderSelectionPath,
                sleepMsBetweenRetries,
                maxRetries);
        // one we have create the connection we should start it to activate the communication
        this.cn.initialize();
        //runner = SpringUtils.ctx.getBean(Runner.class);
        // for application cluster management we need to instantiate ClusterMembers class with the client (this.cn)
        this.cm = new ClusterMembers(this.cn.getClient(),
                groupPath,
                clientName,
                clusterMembers,
                this,
                1, // initial delay for the first check
                1); // period for the upcoming checks
        try {
            // when ready to get the states of the peers in the cluster we need to start it
            this.cm.startCluster();
        } catch (IOException e) {
            logger.error("Exception on cluster init", e);
        }
        // for the leader election in the cluster we need the instantiate LeaderElector with the client (this.cn)
        this.le = new LeaderElector(this.cn.getClient(), leaderSelectionPath, clientName, this);
        try {
            // when ready for leader operations in the application start leader election
            this.le.startLeaderSelector();
        } catch (Exception e) {
            logger.error("Exception on leader init", e);
        }

    }

    @Override
    public void peersTurnedUp(List<String> ids) {
        for (String id : ids) {
            logger.info("{} turned Up", id);
            peersUp.add(id);
        }
    }

    @Override
    public void peersTurnedDown(List<String> ids) {
        for (String id : ids) {
            logger.info("{} Turned Down", id);
            peersDown.add(id);
            peersUp.remove(id);
            doLeaderJob();

        }
    }

    public void wakeUpAllMachinesInDb() {
        MachineRunner machineRunner = SpringUtils.ctx.getBean(MachineRunner.class);
        contextList = machineRunner.exampleRepository.findAll();
        if (contextList.size() != 0) {
            for (int j = 0; j < contextList.size(); j++) {
                TransitionContext iter = contextList.get(j);
                String temp;
                temp = peersUp.get(0);
                iter.setAppId(temp);
                machineRunner.provision(iter.getStateMachineID());
                machineRunner.exampleRepository.save(iter);
            }
            System.out.println("Database updated!");
        }
    }

    @Override
    public boolean doLeaderJob() {
        boolean keepLeaderShip = true;
        if (this.le.amILeader()) { //only leader updates DB
            while (peersUp.size() == 0) ;
            if (peersUp.size() > 1) {
                updateDbOnPeerDown();
            } else {
                wakeUpAllMachinesInDb();
            }
        } else {
            MachineRunner machineRunner = SpringUtils.ctx.getBean(MachineRunner.class);
            while ((machineRunner.exampleRepository.findAllByAppId(peersDown.get(peersDown.size() - 1)) != null) &&
                    (machineRunner.exampleRepository.findAllByAppId(peersDown.get(peersDown.size() - 1)).size() != 0)) ;
            contextList = machineRunner.exampleRepository.findAllByAppId(machineRunner.appId);
            for (int j = 0; j < contextList.size(); j++) {
                TransitionContext iter = contextList.get(j);
                if (iter.getAppId().equals(machineRunner.appId))
                    machineRunner.provision(iter.getStateMachineID());
            }
        }
        return keepLeaderShip;
    }

    public void updateDbOnPeerDown() {
        if (peersDown.size() != 0) { // if there is a downed peer
            String lastDownPeerId = peersDown.get(peersDown.size() - 1);
            logger.info("I am leader now and will change database entities of {} if exist in DB", lastDownPeerId);
            MachineRunner machineRunner = SpringUtils.ctx.getBean(MachineRunner.class);
            contextList = machineRunner.exampleRepository.findAllByAppId(lastDownPeerId);
            if (contextList.size() != 0) {
                for (int j = 0; j < contextList.size(); j++) {
                    TransitionContext iter = contextList.get(j);
                    String temp;
                    if (peersUp.size() > 1)
                        temp = peersUp.get(j % 2);
                    else
                        temp = peersUp.get(0);
                    iter.setAppId(temp);
                    if (iter.getAppId().equals(machineRunner.appId)) {
                        machineRunner.provision(iter.getStateMachineID());
                    }
                    machineRunner.exampleRepository.save(iter);
                }
                System.out.println("Database updated!");
            }
        }
    }

    public LeaderElector getLe() {
        return le;
    }

    public ClusterMembers getCm() {
        return cm;
    }

    public ClientConnector getCn() {
        return cn;
    }

}