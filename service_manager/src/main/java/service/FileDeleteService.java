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
public class FileDeleteService {

    private final SessionScopedJPaxosClient sessionScopedJPaxosClient;

    @Autowired
    public FileDeleteService(@Lazy SessionScopedJPaxosClient sessionScopedJPaxosClient) {
        this.sessionScopedJPaxosClient = sessionScopedJPaxosClient;
    }

    public String deleteFile(int fileId) {
        try {
            // Prepare delete request
            byte[] dataToSend = prepareDeleteRequest(fileId);
            byte[] response = sessionScopedJPaxosClient.getClient().execute(dataToSend);
            return "File marked as inactive successfully: " + new String(response);
        } catch (IOException | ReplicationException e) {
            e.printStackTrace();
            return "File deletion failed: " + e.getMessage();
        }
    }

    /**
     * Prepare delete request for soft deletion.
     *
     * @param fileId The id of the file to delete.
     * @return Byte array representing the request.
     * @throws IOException Throws IOException.
     */
    private byte[] prepareDeleteRequest(int fileId) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        // Write request type code for DELETE
        dos.writeInt(RequestType.DELETE.getCode());

        // Write file ID
        dos.writeInt(fileId);

        return baos.toByteArray();
    }
}
