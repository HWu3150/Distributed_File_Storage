package zk;

import lsr.common.Configuration;
import lsr.paxos.replica.Replica;
import lsr.service.Service;
import org.apache.zookeeper.KeeperException;

import java.util.Timer;
import java.util.TimerTask;


@SuppressWarnings("LombokGetterMayBeUsed")
public class JNode extends Replica {
    String fullPath;
    private final Timer heartbeatTimer;
    ZKConnectionManager zkConnectionManager;

    public JNode(Configuration config, int localId, Service service, String serviceName, String address,
                 ZKConnectionManager zkConnectionManager) throws KeeperException, InterruptedException {
        super(config, localId, service);

        // Register the service in ZooKeeper
        ServiceRegistry serviceRegistry = new ServiceRegistry(zkConnectionManager.getZooKeeper(), serviceName);
        this.fullPath = serviceRegistry.registerService(address);
        this.zkConnectionManager = zkConnectionManager;
        System.out.println("JNode registered at address: " + this.fullPath);

        this.heartbeatTimer = new Timer(true);
        startHeartbeat();
    }

    public String getFullPath() {
        return fullPath;
    }

    private void startHeartbeat() {
        heartbeatTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    // Update the node's data to show it's alive
                    String heartbeatData = "Heartbeat at " + System.currentTimeMillis();
                    zkConnectionManager.getZooKeeper().setData(fullPath, heartbeatData.getBytes(), -1);
                    System.out.println("Heartbeat sent: " + heartbeatData);
                } catch (Exception e) {
                    System.err.println("Failed to send heartbeat: " + e.getMessage());
                }
            }
        }, 0, 3000); // Send heartbeat every 5 seconds
    }

    public void stopHeartbeat() {
        heartbeatTimer.cancel();
        System.out.println("Heartbeat stopped for JNode at: " + fullPath);
    }

    @Override
    public void forceExit() {
        super.forceExit();
        System.out.println("JNode force exited");
        stopHeartbeat();

        try {
            zkConnectionManager.getZooKeeper().delete(fullPath, -1);
            System.out.println("JNode force exited and node explicitly deleted at: " + fullPath);
        } catch (KeeperException | InterruptedException e) {
            System.err.println("Failed to delete node on forceExit: " + e.getMessage());
        }
    }
}

