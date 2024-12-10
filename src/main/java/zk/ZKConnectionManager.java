package zk;

import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.io.FileInputStream;
import java.util.Properties;

@SuppressWarnings("LombokGetterMayBeUsed")
public class ZKConnectionManager {

    private ZooKeeper zooKeeper;
    private CountDownLatch connectionLatch = new CountDownLatch(1);
    private final int sessionTimeout;
    private final String host;

    public ZKConnectionManager() throws IOException {
        Properties properties = new Properties();
        try (FileInputStream in = new FileInputStream("zk.properties")) {
            properties.load(in);
            this.sessionTimeout = Integer.parseInt(properties.getProperty("sessionTimeout", "5000"));
            this.host = properties.getProperty("host", "localhost");
        }
    }

    // Establish a connection
    public ZooKeeper connect() throws IOException, InterruptedException {
        try {
            zooKeeper = new ZooKeeper(this.host, this.sessionTimeout, event -> {
                if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                    connectionLatch.countDown();
                }
            });
            connectionLatch.await();
            return zooKeeper;
        } catch (IOException | InterruptedException e) {
            close();
            throw e;
        }
    }


    // Close connection
    public void close() throws InterruptedException {
        if (zooKeeper != null) {
            zooKeeper.close();
        }
    }

    public ZooKeeper getZooKeeper() {
        return zooKeeper;
    }
}


