package zk;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

public class NodeUtility {

    private final ZooKeeper zooKeeper;

    public NodeUtility(ZooKeeper zooKeeper) {
        this.zooKeeper = zooKeeper;
    }

    public void createNode(String path, byte[] data, CreateMode createMode) throws KeeperException, InterruptedException {
        if (zooKeeper.exists(path, false) == null) {
            zooKeeper.create(
                    path,
                    data,
                    ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    createMode
            );
        }
    }

    public void deleteNode(String path) throws KeeperException, InterruptedException {
        if (zooKeeper.exists(path, false) != null) {
            zooKeeper.delete(path, -1);
        }
    }

    public byte[] getNodeData(String path, Watcher watcher) throws KeeperException, InterruptedException {
        return zooKeeper.getData(path, watcher, null);
    }

    public void setNodeData(String path, byte[] data) throws KeeperException, InterruptedException {
        if (zooKeeper.exists(path, false) != null) {
            zooKeeper.setData(path, data, -1);
        } else {
            createNode(path, data, CreateMode.PERSISTENT);
        }
    }

    public Stat nodeExists(String path, Watcher watcher) throws KeeperException, InterruptedException {
        return zooKeeper.exists(path, watcher);
    }
}
