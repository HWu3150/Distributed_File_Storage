import lsr.common.Configuration;
import lsr.paxos.client.Client;
import lsr.paxos.client.ReplicationException;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.apache.commons.cli.*;;

import io.netty.handler.stream.ChunkedFile;

import java.io.IOException;

enum Operation {
    READ, WRITE
}

public class TestClient {
    public static void main(String[] args) {
        String filePath;
        Operation op = Operation.WRITE;

        if (args.length < 2) {
            System.out.println("Usage: java TestClient <file_path> <op>");
            System.exit(1);
        }

        Options options = new Options();
        options.addOption("f", "file", true, "File path to process");
        options.addOption("r", "read", false, "Read file");
        options.addOption("w", "write", false, "Write file");
        options.addOption("h", "help", false, "Display help message");

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();

        try {
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption("h")) {
                formatter.printHelp("CommandLineArgsExample", options);
                return;
            }

            if (cmd.hasOption("f")) {
                String filePath = cmd.getOptionValue("f");
            } else {
                formatter.printHelp("CommandLineArgsExample", options);
                return;
            }

            if (cmd.hasOption("r")) {
                op = Operation.READ;
            } else if (cmd.hasOption("w")) {
                op = Operation.WRITE;
            }

        } catch (ParseException e) {
            System.out.println("Error parsing arguments: " + e.getMessage());
            formatter.printHelp("CommandLineArgsExample", options);
            return;
        }

        Path path = Paths.get(filePath);
        String fileName = path.getFileName().toString();

        try {
            Configuration config = new Configuration("jpaxos.properties");

            // create client instance
            Client client = new Client(config);

            // start client
            client.connect();
            
            // only support write now
            List<FileChunk> chunks = FileSplitter.splitFile(filePath);
            for (FileChunk chunk : chunks) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(baos);

                dos.writeInt(fileName.length());
                dos.write(fileName);
                dos.writeInt(chunk.getChunkIndex());
                dos.writeLong(chunk.getStartOffset());
                dos.writeLong(chunk.getEndOffset());
                dos.writeLong(chunk.getSize());
                dos.write(chunk.getData());
        
                client.execute(baos.toByteArray());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
