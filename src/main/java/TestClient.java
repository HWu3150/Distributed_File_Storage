import lsr.common.Configuration;
import lsr.paxos.client.Client;
import lsr.paxos.client.ReplicationException;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;

public class TestClient {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java TestClient <file_path>");
            System.exit(1);
        }

        String filePath = args[0];

        try {
            Configuration config = new Configuration("jpaxos.properties");

            // create client instance
            Client client = new Client(config);

            // start client
            client.connect();

            Path path = Paths.get(filePath);

            // get file in the form of byte array
            byte[] fileContent = Files.readAllBytes(path);

            String fileName = path.getFileName().toString();

            byte[] dataToSend = dataToByteArr(fileName, fileContent);

            // send request to replica
            byte[] response = client.execute(dataToSend);

            System.out.println("Response received: " + new String(response));
        } catch (IOException | ReplicationException e) {
            e.printStackTrace();
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
