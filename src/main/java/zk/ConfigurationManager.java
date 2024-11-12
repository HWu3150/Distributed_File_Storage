package zk;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

public class ConfigurationManager {

    private static final String CONFIG_PATH = "/jpaxos/config";
    private NodeUtility nodeUtility;

    public ConfigurationManager(ZooKeeper zooKeeper) {
        this.nodeUtility = new NodeUtility(zooKeeper);
    }

    public void updateConfig(String configData) throws KeeperException, InterruptedException {
        if (nodeUtility.nodeExists(CONFIG_PATH, null) == null) {
            nodeUtility.createNode(CONFIG_PATH, configData.getBytes(), CreateMode.PERSISTENT);
        } else {
            nodeUtility.setNodeData(CONFIG_PATH, configData.getBytes());
        }
    }

    public String getConfig() throws KeeperException, InterruptedException {
        byte[] data = nodeUtility.getNodeData(CONFIG_PATH, null);
        return new String(data);
    }

    public void watchConfigChanges(Watcher watcher) throws KeeperException, InterruptedException {
        nodeUtility.getNodeData(CONFIG_PATH, watcher);
    }
}
