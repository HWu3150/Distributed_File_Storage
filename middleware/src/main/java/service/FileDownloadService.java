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
public class FileDownloadService {
    private final SessionScopedJPaxosClient sessionScopedJPaxosClient;

    @Autowired
    public FileDownloadService(@Lazy SessionScopedJPaxosClient sessionScopedJPaxosClient) {
        this.sessionScopedJPaxosClient = sessionScopedJPaxosClient;
    }

    public byte[] downloadFile(String fileName) {
        try {
            byte[] request = prepareDownloadRequest(fileName);
            return sessionScopedJPaxosClient.getClient().execute(request);
        } catch (IOException | ReplicationException e) {
            e.printStackTrace();
            throw new RuntimeException("File download failed: " + e.getMessage());
        }
    }

    /**
     * Prepare download request.
     *
     * @param fileName Name of the file.
     * @return Download request in byte array form.
     * @throws IOException Throws IOException.
     */
    private byte[] prepareDownloadRequest(String fileName) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(RequestType.DOWNLOAD.getCode());
        dos.writeInt(fileName.length());
        dos.writeBytes(fileName);
        return baos.toByteArray();
    }
}
