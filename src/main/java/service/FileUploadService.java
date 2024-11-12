package service;

import lsr.common.Configuration;
import lsr.paxos.client.Client;
import lsr.paxos.client.ReplicationException;
import lsr.paxos.replica.Replica;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

@Service
public class FileUploadService {
    private Client client;

    public FileUploadService() {}

    @PostConstruct
    public void initClient() throws IOException {
        Configuration config = new Configuration("jpaxos.properties");
        this.client = new Client(config);
        this.client.connect();
    }

    public String uploadFile(String fileName, byte[] fileContent) {
        try {
            byte[] dataToSend = dataToByteArr(fileName, fileContent);
            byte[] response = client.execute(dataToSend);
            return "Response received: " + new String(response);
        } catch(IOException |ReplicationException e) {
            e.printStackTrace();
            return "File upload failed: " + e.getMessage();
        }
    }

    private static byte[] dataToByteArr(String fileName, byte[] fileContent) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

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
