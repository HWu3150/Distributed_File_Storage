package zk;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

public class ZKWatcher implements Watcher {

    private final EventListener eventListener;

    public ZKWatcher(EventListener eventListener) {
        this.eventListener = eventListener;
    }

    @Override
    public void process(WatchedEvent event) {
        switch (event.getType()) {
            case NodeCreated:
                eventListener.onNodeCreated(event.getPath());
                break;
            case NodeDeleted:
                eventListener.onNodeDeleted(event.getPath());
                break;
            case NodeDataChanged:
                eventListener.onDataChanged(event.getPath());
                break;
            case NodeChildrenChanged:
                eventListener.onChildrenChanged(event.getPath());
                break;
            default:
                break;
        }
    }
}
