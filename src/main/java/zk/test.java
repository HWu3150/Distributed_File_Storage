package zk;

import Jpaxos.FileStorageService;
import lsr.common.Configuration;
import org.apache.zookeeper.KeeperException;

import java.io.IOException;
import java.util.List;

public class test {
    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
        ZKConnectionManager zkConnectionManager = new ZKConnectionManager();
        zkConnectionManager.connect();

        String serviceName = "distributed_file_system";

        ServiceRegistry serviceRegistry = new ServiceRegistry(zkConnectionManager.getZooKeeper(), serviceName);
//        serviceRegistry.registerService("127.0.0.1:7089");
//        serviceRegistry.registerService("127.0.0.1:7090");
//        serviceRegistry.registerService("127.0.0.1:7091");



        int replicaId = 0;
        Configuration config = new Configuration("jpaxos.properties");
        JNode jNode = new JNode(config, replicaId, new FileStorageService(replicaId), serviceName, "127.0.0.1:7089", zkConnectionManager);



        ServiceDiscovery serviceDiscovery = new ServiceDiscovery(zkConnectionManager.getZooKeeper(), serviceName);
        List<String> list = serviceDiscovery.discoverServices();
        System.out.println(list);

    }
}
