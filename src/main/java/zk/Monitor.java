package zk;


import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

public class Monitor implements Watcher {
    private final ZooKeeper zooKeeper;
    private final String jNodePath;

    public Monitor(ZooKeeper zooKeeper, String jNodePath) {
        this.zooKeeper = zooKeeper;
        this.jNodePath = jNodePath;
    }


    public void startMonitoring() throws KeeperException, InterruptedException {
        // Check if node exists and set a watch
        Stat stat = zooKeeper.exists(jNodePath, this);
        if (stat == null) {
            System.out.println("JNode not active at path: " + jNodePath);
        } else {
            System.out.println("Started monitoring JNode at: " + jNodePath);
        }
    }


    @Override
    public void process(WatchedEvent event) {
        if (event.getType() == Event.EventType.NodeDeleted && event.getPath().equals(jNodePath)) {
            // The ephemeral node is gone, hence JNode has disconnected
            System.out.println("JNode has been disconnected. (Heartbeat node deleted at: " + jNodePath + ")");
            try {
                zooKeeper.delete(jNodePath, -1);
            } catch (InterruptedException | KeeperException e) {
                throw new RuntimeException(e);
            }
        }
    }
}