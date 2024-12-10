package zk;

import org.apache.zookeeper.KeeperException;

import java.io.IOException;
import java.util.List;

public class Test {
    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
        ZKConnectionManager zkConnectionManager = new ZKConnectionManager();
        zkConnectionManager.connect();
        ServiceRegistry serviceRegistry = new ServiceRegistry(zkConnectionManager.getZooKeeper(), "distributed_file_system");
        serviceRegistry.registerService("127.0.0.1:7089");
        serviceRegistry.registerService("127.0.0.1:70001");
        serviceRegistry.registerService("127.0.0.1:70002");

        ServiceDiscovery serviceDiscovery = new ServiceDiscovery(zkConnectionManager.getZooKeeper(), "distributed_file_system");
        List<String> list = serviceDiscovery.discoverServices();
        System.out.println(list);

    }
}
