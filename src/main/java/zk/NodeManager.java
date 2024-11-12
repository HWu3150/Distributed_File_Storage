package zk;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;

import java.io.IOException;

public class NodeManager {

    private final ZKConnectionManager connectionManager;
    private NodeUtility nodeUtility;
    private String nodePath;

    public NodeManager(String nodeId) throws InterruptedException, IOException {
        connectionManager = new ZKConnectionManager();
        connectionManager.connect();
        nodeUtility = new NodeUtility(connectionManager.getZooKeeper());
        nodePath = "/nodes/" + nodeId;
    }

    public void registerNode(String data) throws KeeperException, InterruptedException {
        nodeUtility.createNode(nodePath, data.getBytes(), CreateMode.EPHEMERAL);
    }

    public void updateNodeData(String data) throws KeeperException, InterruptedException {
        nodeUtility.setNodeData(nodePath, data.getBytes());
    }

    public void removeNode() throws KeeperException, InterruptedException {
        nodeUtility.deleteNode(nodePath);
    }

    public void watchNode(Watcher watcher) throws KeeperException, InterruptedException {
        nodeUtility.nodeExists(nodePath, watcher);
    }

    public void close() throws InterruptedException {
        connectionManager.close();
    }
}
