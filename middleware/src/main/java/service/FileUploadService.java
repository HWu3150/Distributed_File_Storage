package service;

import client.SessionScopedJPaxosClient;
import db.DBClient;
import db.FileEntity;
import lsr.paxos.client.ReplicationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

@Service
public class FileUploadService {
    private final SessionScopedJPaxosClient sessionScopedJPaxosClient;
    private final DBClient dbClient;

    @Autowired
    public FileUploadService(@Lazy SessionScopedJPaxosClient sessionScopedJPaxosClient, DBClient dbClient) {
        this.sessionScopedJPaxosClient = sessionScopedJPaxosClient;
        this.dbClient = dbClient;
    }

    public String uploadFile(String fileName, byte[] fileContent) {
        try {
            // Store file
            byte[] dataToSend = prepareUploadRequest(fileName, fileContent);
            byte[] fileURL = sessionScopedJPaxosClient.getClient().execute(dataToSend);
            // Store file metadata in DB
            storeFileMetadata(fileName, fileContent, Arrays.toString(fileURL));
            return "Response received and stored at: " + new String(fileURL);
        } catch(IOException | ReplicationException e) {
            e.printStackTrace();
            return "File upload failed: " + e.getMessage();
        }
    }

    /**
     * Prepare upload request.
     *
     * @param fileName Name of the file.
     * @param fileContent File content.
     * @return Upload request in byte array form.
     * @throws IOException Throws IOException.
     */
    private byte[] prepareUploadRequest(String fileName,
                                        byte[] fileContent) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        // write request type code
        dos.writeInt(RequestType.UPLOAD.getCode());

        // write file name
        dos.writeInt(fileName.length());
        dos.writeBytes(fileName);

        // write file content
        // [file name length][file name][file content length][file content]
        dos.writeInt(fileContent.length);
        dos.write(fileContent);

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
     * Store file metadata.
     *
     * @param fileName Name of the file.
     * @param fileContent File content.
     * @param fileUrl File storage path.
     */
    private void storeFileMetadata(String fileName, byte[] fileContent, String fileUrl) {
        FileEntity fileEntity = FileEntity.builder()
                .fileName(fileName)
                .fileType(getFileType(fileName))
                .fileDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()))
                .fileSize((long) fileContent.length)
                .fileUrl(fileUrl)
                .build();
        dbClient.insert(fileEntity);
    }

}
