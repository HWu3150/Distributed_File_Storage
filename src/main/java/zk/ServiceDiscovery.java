package zk;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ServiceDiscovery {
    private static final Logger log = LoggerFactory.getLogger(ServiceDiscovery.class);
    private final ZooKeeper zooKeeper;
    private String servicePath;
    private List<String> serviceAddresses;

    public ServiceDiscovery(ZooKeeper zooKeeper, String serviceName) {
        this.zooKeeper = zooKeeper;
        this.servicePath = "/services/" + serviceName;
    }

    public List<String> discoverServices() throws KeeperException, InterruptedException {
        updateServices();

        zooKeeper.getChildren(servicePath, event -> {
            if (event.getType() == Watcher.Event.EventType.NodeChildrenChanged) {
                try {
                    updateServices();
                } catch (KeeperException | InterruptedException e) {
                    e.printStackTrace();
//                    log.error(e.getMessage(), e);
                }
            }
        });

        return serviceAddresses;
    }

    private void updateServices() throws KeeperException, InterruptedException {
        serviceAddresses = zooKeeper.getChildren(servicePath, false);
    }
}
