package zk;

public interface EventListener {

    void onNodeCreated(String path);

    void onNodeDeleted(String path);

    void onDataChanged(String path);

    void onChildrenChanged(String path);
}
