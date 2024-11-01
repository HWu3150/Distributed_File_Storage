import lsr.common.Configuration;
import lsr.paxos.client.Client;
import lsr.paxos.client.ReplicationException;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

public class TestClient {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java JPaxosTestClient <file_path>");
            System.exit(1);
        }

        String filePath = args[0];

        try {
            Configuration config = new Configuration("jpaxos.properties");

            // create client instance
            Client client = new Client(config);

            // start client
            client.connect();

            // get file in the form of byte array
            byte[] fileContent = Files.readAllBytes(Paths.get(filePath));

            // send request to replica
            byte[] response = client.execute(fileContent);

            System.out.println("Response received: " + new String(response));
        } catch (IOException | ReplicationException e) {
            e.printStackTrace();
        }
    }
}
