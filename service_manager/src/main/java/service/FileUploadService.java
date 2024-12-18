package service;

import client.SessionScopedJPaxosClient;
import common.RequestType;
import lsr.paxos.client.ReplicationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

@Service
public class FileUploadService {
    
    private final SessionScopedJPaxosClient sessionScopedJPaxosClient;

    @Autowired
    public FileUploadService(@Lazy SessionScopedJPaxosClient sessionScopedJPaxosClient) {
        this.sessionScopedJPaxosClient = sessionScopedJPaxosClient;
    }

    public String uploadFile(String fileName, byte[] fileContent) {
        try {
            // Store file
            byte[] dataToSend = prepareUploadRequest(fileName, fileContent);
            byte[] fileURL = sessionScopedJPaxosClient.getClient().execute(dataToSend);
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

}
