package service;

import jpaxos.RequestType;
import lsr.paxos.client.Client;
import lsr.common.Configuration;
import lsr.paxos.client.ReplicationException;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

@Service
public class FileDownloadService {
    private Client client;

    public FileDownloadService() {}

    @PostConstruct
    public void initClient() throws IOException {
        Configuration config = new Configuration("jpaxos.properties");
        this.client = new Client(config);
        this.client.connect();
    }

    public byte[] downloadFile(String fileName) {
        try {
            byte[] request = prepareDownloadRequest(fileName);
            return client.execute(request);
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
        dos.write(RequestType.DOWNLOAD.getCode());
        dos.writeBytes(fileName);
        return baos.toByteArray();
    }
}
