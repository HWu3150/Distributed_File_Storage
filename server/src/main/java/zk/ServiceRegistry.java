package zk;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@SuppressWarnings("CallToPrintStackTrace")
public class ServiceRegistry {
    private static final Logger log = LoggerFactory.getLogger(ServiceRegistry.class);
    private final ZooKeeper zooKeeper;
    private final String servicePath;

    public ServiceRegistry(ZooKeeper zooKeeper, String serviceName) {
        this.zooKeeper = zooKeeper;
        this.servicePath = "/services/" + serviceName;
        createServiceRootNode();
    }

    private void createServiceRootNode() {
        try {

            String parentPath = "/services";
            Stat parentStat = zooKeeper.exists(parentPath, false);
            if (parentStat == null) {
                zooKeeper.create(parentPath, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }

            Stat stat = zooKeeper.exists(servicePath, false);
            if (stat == null) {
                zooKeeper.create(servicePath, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }



    public String registerService(String address) throws KeeperException, InterruptedException {
        String fullPath = servicePath + "/" + address;
        return zooKeeper.create(fullPath, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
    }
}
