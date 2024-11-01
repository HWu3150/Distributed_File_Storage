import lsr.service.SimplifiedService;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileStorageService extends SimplifiedService{

    private static final String STORAGE_DIR = "replicated_files";
    private final Integer replicaId;

    public FileStorageService(int replicaId) {
        File storageDir = new File(STORAGE_DIR);
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
        this.replicaId = replicaId;
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
            File file = new File(STORAGE_DIR, "replicated_file_" + replicaId + System.currentTimeMillis());
            Files.write(file.toPath(), value);
            System.out.println("File has been stored at: " + file.getAbsolutePath());
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
            oos.writeObject(new File(STORAGE_DIR).listFiles());
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
                Files.copy(file.toPath(), Paths.get(STORAGE_DIR, file.getName()));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to restore snapshot", e);
        }
    }
}
