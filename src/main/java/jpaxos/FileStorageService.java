package jpaxos;

import lsr.service.SimplifiedService;
import model.FileData;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileStorageService extends SimplifiedService{

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
        try {
            FileData fileData = parseFileData(value);
            File file = new File(storage_dir, fileData.getFileName());
            Files.write(file.toPath(), fileData.getFileContent());
            System.out.println("File: " + fileData.getFileName() + " has been stored at: " + file.getAbsolutePath());
            return "File received and stored.".getBytes();
        } catch (IOException e) {
            e.printStackTrace();
            return "Failed to store file.".getBytes();
        }
    }

    /**
     * Makes snapshot of current service state (list of files in the current replica).
     *
     * @return Byte array of the snapshot.
     */
    @Override
    protected byte[] makeSnapshot() {
        try{
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
     * Update the current state of service to state from snapshot for recovery & catch-up purposes.
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
     * Parse the file data sent from the client, from byte array to model.FileData object.
     *
     * @param data File data sent from the client in the form of a byte array.
     * @return An intermediate model.FileData object which stores metadata and content of the file.
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
