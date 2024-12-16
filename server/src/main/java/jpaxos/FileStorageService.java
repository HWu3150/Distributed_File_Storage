package jpaxos;

import common.FileDTO;
import common.RequestType;
import db.DBClient;
import db.FileEntity;
import lsr.service.SimplifiedService;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FileStorageService extends SimplifiedService {

    private static final String STORAGE_DIR_PREFIX = "replicated_files";
    private final Integer replicaId;
    private final String storage_dir;
    private final DBClient dbClient;

    public FileStorageService(int replicaId, DBClient dbClient) {
        this.replicaId = replicaId;
        this.storage_dir = STORAGE_DIR_PREFIX + "_replica_" + replicaId;
        this.dbClient = dbClient;

        File storageDir = new File(storage_dir);
        if (!storageDir.exists()) {
            boolean succeed = storageDir.mkdirs();
            if (!succeed) {
                throw new RuntimeException("Failed to create storage directory.");
            }
        }
    }

    /**
     * Process client request.
     *
     * @param value Request from the client.
     * @return Execution result.
     */
    @Override
    protected byte[] execute(byte[] value) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(value);
            DataInputStream dis = new DataInputStream(bais);
            int requestTypeCode = dis.readInt();
            RequestType requestType = getRequestTypeByCode(requestTypeCode);

            switch (requestType) {
                case DOWNLOAD:
                    return handleDownload(dis);
                case UPLOAD:
                    return handleUpload(dis);
                case DELETE:
                    return handleDelete(dis);
                case LIST:
                    return handleList();
                default:
                    throw new IllegalArgumentException("Invalid request type code: "
                            + requestTypeCode + ". Expecting 0: " +
                            "Download, 1: Upload, 2: Delete.");
            }
        } catch (IOException | IllegalArgumentException e) {
            e.printStackTrace();
            return e.getMessage().getBytes();
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
     * Handle file download request.
     *
     * @param dis Data input stream.
     * @return File content in byte array form.
     * @throws IOException Throws IOException.
     */
    private byte[] handleDownload(DataInputStream dis) throws IOException {
        // Read file name
        int fileNameLength = dis.readInt();
        byte[] fileNameBytes = new byte[fileNameLength];
        dis.readFully(fileNameBytes);
        String fileName = new String(fileNameBytes);

        // Read file content
        byte[] fileContent = readFileFromStorage(fileName);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.write(fileContent);

        return baos.toByteArray();
    }

    /**
     * Handle file upload request.
     *
     * @param dis Data input stream.
     * @return File URL.
     * @throws IOException Throws IOException.
     */
    private byte[] handleUpload(DataInputStream dis) throws IOException {
        // Store file
        FileData fileData = parseFileData(dis);
        String fileName = fileData.getFileName();
        File file = new File(storage_dir, fileName);
        Files.write(file.toPath(), fileData.getFileContent());
        System.out.println("File: " + fileData.getFileName() + " has been stored at: " + file.getAbsolutePath());

        // Store file metadata
        FileEntity fileEntity = FileEntity.builder()
                .fileName(fileName)
                .fileType(getFileType(fileName))
                .fileDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()))
                .fileSize((long) fileData.getFileContent().length)
                .fileUrl(file.getAbsolutePath())
                .build();
        dbClient.insert(replicaId, fileEntity);
        System.out.println("File metadata has been stored in replica_" + replicaId + "'s database.");

        return file.getAbsolutePath().getBytes();
    }

    /**
     * Handle file delete request.
     *
     * @param dis Data input stream.
     * @return Deletion successful/failed.
     * @throws IOException Throws IOException.
     */
    private byte[] handleDelete(DataInputStream dis) throws IOException {
        return null;
    }

    /**
     * Handle list files request.
     *
     * @return List of files in byte array form.
     */
    private byte[] handleList() throws IOException{
        List<FileDTO> fileDTOs = new ArrayList<>();
        List<FileEntity> fileEntities = dbClient.getAll(replicaId);
        for (FileEntity entity : fileEntities) {
            fileDTOs.add(FileDTO.builder()
                    .fileName(entity.getFileName())
                    .fileType(entity.getFileType())
                    .fileDate(entity.getFileDate())
                    .fileSize(entity.getFileSize())
                    .build()
            );
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(fileDTOs);
        oos.close();
        return baos.toByteArray();
    }

    /**
     * Get file type.
     *
     * @param fileName Name of the file.
     * @return File type.
     */
    private String getFileType(String fileName) {
        int index = fileName.lastIndexOf('.');
        return (index > 0) ? fileName.substring(index + 1) : "unknown";
    }

    /**
     * Get type of the request by its code.
     *
     * @param code Code of the request.
     * @return Type of the request.
     */
    private static RequestType getRequestTypeByCode(int code) {
        for (RequestType type : RequestType.values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        return RequestType.UNKNOWN;
    }

    /**
     * Read file content from local storage by file name.
     *
     * @param fileName Name of the file.
     * @return File content in byte array form.
     * @throws IOException Throws IOException.
     */
    private byte[] readFileFromStorage(String fileName) throws IOException {
        Path filePath = Paths.get(storage_dir, fileName);
        if (!Files.exists(filePath)) {
            throw new IOException("File not found: " + filePath);
        }
        return Files.readAllBytes(filePath);
    }

    /**
     * Parse the file data sent from the client, from data input stream to model.FileData object.
     *
     * @param dis File data sent from the client in the form of data input stream.
     * @return An intermediate model.FileData object which stores metadata and content of the file.
     * @throws IOException Throws IOException.
     */
    private FileData parseFileData(DataInputStream dis) throws IOException {
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
