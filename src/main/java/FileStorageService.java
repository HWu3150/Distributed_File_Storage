import lsr.service.SimplifiedService;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class FileStorageService extends SimplifiedService {

    private static final String STORAGE_DIR_PREFIX = "replicated_files";
    private final Integer replicaId;
    private final String storage_dir;

    public FileStorageService(int replicaId) {
        this.replicaId = replicaId;
        this.storage_dir = STORAGE_DIR_PREFIX + "_replica_" + replicaId;

        File storageDir = new File(storage_dir);
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
    }

    /**
     * Stores file at current replica.
     *
     * @param value File to be stored in the form a byte array.
     * @return Whether file has been successfully stored.
     */
    @Override
    protected byte[] execute(byte[] value) {
        Message msg = Message.fromBytes(value);
        switch (msg.getOp()) {
            case PUT:
                return putFile(msg.getFileName(), msg.getData());
            case GET:
                return getFile(msg.getFileName());
            case DELETE:
                return deleteFile(msg.getFileName());
            case LIST:
                return listFiles();
            default:
                return "Invalid operation".getBytes();
        }
    }

    private byte[] putFile(String fileName, byte[] data) {
        File file = new File(storage_dir, fileName);
        PutResp resp;
        try {
            Files.write(file.toPath(), data);
        } catch (IOException e) {
            e.printStackTrace();
            resp = new PutResp(false, e.getMessage());
        }
        resp = new PutResp(true, "success");
        return resp.toBytes();
    }

    private byte[] getFile(String fileName) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int chunkIndex = 0;
        GetResp resp;

        while (true) {
            String chunkFileName = fileName + "-" + chunkIndex;
            File chunkFile = new File(storage_dir, chunkFileName);
            System.out.println("Reading chunk file: " + chunkFile.toPath());

            if (chunkFile.exists()) {
                try {
                    byte[] chunkData = Files.readAllBytes(chunkFile.toPath());
                    System.out.println("Read chunk data: " + chunkData.length);
                    baos.write(chunkData);
                    chunkIndex++;
                } catch (IOException e) {
                    e.printStackTrace();
                    resp = new GetResp(null, false, e.getMessage());
                    break;
                }
            } else {
                break;
            }
        }
        resp = new GetResp(baos.toByteArray(), true, "success");
        System.out.println("Read file data: " + resp.getData().length);
        return resp.toBytes();
    }

    private byte[] deleteFile(String fileName) {
        DeleteResp resp;
        try {
            Files.walk(Paths.get(storage_dir))
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().startsWith(fileName + "-"))
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
            resp = new DeleteResp(false, e.getMessage());
        }
        resp = new DeleteResp(true, "success");
        return resp.toBytes();
    }

    private byte[] listFiles() {
        ListResp resp;
        ArrayList<String> list_files = new ArrayList<>();
        try {
            Map<String, List<Path>> dedup_files = Files.walk(Paths.get(storage_dir))
                .filter(Files::isRegularFile)
                .collect(Collectors.groupingBy(path -> {
                    String filename = path.getFileName().toString();
                    int separatorIndex = filename.indexOf("-");
                    return (separatorIndex != -1) ? filename.substring(0, separatorIndex) : filename;
                }));
            dedup_files.forEach((prefix, files) -> {
                list_files.add(prefix);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        resp = new ListResp(list_files);
        return resp.toBytes();
    }

    /**
     * Makes snapshot of current service state (list of files in the current
     * replica).
     *
     * @return Byte array of the snapshot.
     */
    @Override
    protected byte[] makeSnapshot() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(new File(storage_dir).listFiles());
            oos.close();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Snapshot creation failed", e);
        }
    }

    /**
     * Update the current state of service to state from snapshot for recovery &
     * catch-up purposes.
     *
     * @param snapshot Byte array of the snapshot.
     */
    @Override
    protected void updateToSnapshot(byte[] snapshot) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(snapshot);
            ObjectInputStream ois = new ObjectInputStream(bais);
            File[] files = (File[]) ois.readObject();
            for (File file : files) {
                Files.copy(file.toPath(), Paths.get(storage_dir, file.getName()));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to restore snapshot", e);
        }
    }

    /**
     * Parse the file data sent from the client, from byte array to FileData object.
     *
     * @param data File data sent from the client in the form of a byte array.
     * @return An intermediate FileData object which stores metadata and content of
     *         the file.
     * @throws IOException Throws IOException.
     */
    private static FileData parseFileData(byte[] data) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        DataInputStream dis = new DataInputStream(bais);

        // read file name and its length
        int fileNameLength = dis.readInt();
        byte[] fileNameBytes = new byte[fileNameLength];
        dis.readFully(fileNameBytes);
        String fileName = new String(fileNameBytes, StandardCharsets.UTF_8);

        // read file content and its length
        int fileContentLength = dis.readInt();
        byte[] fileContent = new byte[fileContentLength];
        dis.readFully(fileContent);

        return new FileData(fileName, fileContent);
    }
}
