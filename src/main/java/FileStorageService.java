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
