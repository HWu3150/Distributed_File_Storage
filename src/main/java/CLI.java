import org.apache.commons.cli.*;

enum Operation {
    PUT, GET, DELETE, LIST
}

public class CLI {
    public static void main(String[] args) {
        String filePath;
        Operation op = Operation.PUT;
        String savePath = null;

        Options options = new Options();
        options.addOption("f", "file", true, "File path to process");
        options.addOption("p", "put", false, "Put file");
        options.addOption("g", "get", true, "Get file");
        options.addOption("d", "delete", false, "Delete file");
        options.addOption("l", "list", false, "List files");
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
                filePath = cmd.getOptionValue("f");
            } else {
                formatter.printHelp("CommandLineArgsExample", options);
                return;
            }

            if (cmd.hasOption("p")) {
                op = Operation.PUT;
            } else if (cmd.hasOption("g")) {
                op = Operation.GET;
                savePath = cmd.getOptionValue("g");
            } else if (cmd.hasOption("d")) {
                op = Operation.DELETE;
            } else if (cmd.hasOption("l")) {
                op = Operation.LIST;
            }

        } catch (ParseException e) {
            System.out.println("Error parsing arguments: " + e.getMessage());
            formatter.printHelp("CommandLineArgsExample", options);
            return;
        }

        switch (op) {
            case PUT:
                FSClient.putFile(filePath);
                break;
            case GET:
                FSClient.getFile(filePath, savePath);
                break;
            case DELETE:
                FSClient.deleteFile(filePath);
                break;
            case LIST:
                FSClient.listFiles(filePath);
                break;
            default:
                break;
        }
    }
}
