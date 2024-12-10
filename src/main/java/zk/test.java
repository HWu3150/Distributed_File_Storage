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

        Configuration config = new Configuration("jpaxos.properties");
        JNode jNode = new JNode(config, 0, new FileStorageService(0), serviceName, "NODE0", zkConnectionManager);

        jNode.start();
        Monitor monitor0 = new Monitor(zkConnectionManager.getZooKeeper(), jNode.getFullPath());
        monitor0.startMonitoring();

        ServiceDiscovery serviceDiscovery = new ServiceDiscovery(zkConnectionManager.getZooKeeper(), serviceName);
        List<String> list = serviceDiscovery.discoverServices();
        System.out.println(list);


        Thread.sleep(6000);
        System.out.println("simulate start...");
        jNode.forceExit();


        ServiceDiscovery serviceDiscovery1 = new ServiceDiscovery(zkConnectionManager.getZooKeeper(), serviceName);
        List<String> list1 = serviceDiscovery1.discoverServices();
        System.out.println(list1);

    }
}
