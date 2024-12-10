package zk;

import lsr.common.Configuration;
import lsr.paxos.replica.Replica;
import lsr.service.Service;
import org.apache.zookeeper.KeeperException;


public class JNode extends Replica {

    public JNode(Configuration config, int localId, Service service, String serviceName, String address,
                 ZKConnectionManager zkConnectionManager) throws KeeperException, InterruptedException {
        super(config, localId, service);

        // Register the service in ZooKeeper
        ServiceRegistry serviceRegistry = new ServiceRegistry(zkConnectionManager.getZooKeeper(), serviceName);
        serviceRegistry.registerService(address);
        System.out.println("JNode registered at address: " + address);
    }

}

