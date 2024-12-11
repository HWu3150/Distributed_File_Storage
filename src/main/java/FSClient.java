import lsr.common.Configuration;
import lsr.paxos.client.Client;
import lsr.paxos.client.ReplicationException;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.util.List;
import java.io.IOException;
import java.nio.file.Files;

public class FSClient {
    private Client paxos_client;
    private static FSClient instance = null;

    private static FSClient getInstance() {
        if (instance == null) {
            try {
                instance = new FSClient();
                instance.paxos_client = new Client(new Configuration("jpaxos.properties"));
                instance.paxos_client.connect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return instance;
    }

    private byte[] execute(byte[] data) {
        try {
            return paxos_client.execute(data);
        } catch (ReplicationException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void putFile(String filePath) {
        FSClient fsClient = getInstance();
        List<FileChunk> chunks = FileSplitter.splitFile(filePath);
        for (FileChunk chunk : chunks) {
            Message msg = new Message(Message.OpCode.PUT, chunk.getFileName(), chunk.getData());
            byte[] buf = fsClient.execute(msg.toBytes());
            PutResp resp = PutResp.fromBytes(buf);
            if (!resp.isSuccess()) {
                System.out.println("Failed to put file: " + resp.getError());
                return;
            }
        }
    }

    public static void getFile(String filePath, String savePath) {
        FSClient fsClient = getInstance();
        Message msg = new Message(Message.OpCode.GET, filePath, null);
        byte[] buf = fsClient.execute(msg.toBytes());
        GetResp resp = GetResp.fromBytes(buf);
        if (!resp.isSuccess()) {
            System.out.println("Failed to get file: " + resp.getError());
            return;
        }
        File file = new File(savePath);
        try {
            Files.write(file.toPath(), resp.getData());
            System.out.println("data size " + resp.getData().length);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("File saved successfully");
    }

    public static void deleteFile(String filePath) {
        FSClient fsClient = getInstance();
        Message msg = new Message(Message.OpCode.DELETE, filePath, null);
        byte[] buf = fsClient.execute(msg.toBytes());
        DeleteResp resp = DeleteResp.fromBytes(buf);
        if (!resp.isSuccess()) {
            System.out.println("Failed to delete file: " + resp.getError());
        }
        System.out.println("File deleted successfully");
    }

    public static void listFiles(String filePath) {
        FSClient fsClient = getInstance();
        Message msg = new Message(Message.OpCode.LIST, null, null);
        byte[] buf = fsClient.execute(msg.toBytes());
        ListResp resp = ListResp.fromBytes(buf);
        for (String file : resp.getFiles()) {
            System.out.println("list file: " + file);
        }

    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java TestClient <file_path>");
            System.exit(1);
        }

    }
}
