import lsr.common.Configuration;
import lsr.paxos.replica.Replica;

public class JPaxosReplica {

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("Usage: java JPaxosReplica <replica_id>");
            System.exit(1);
        }

        // get replica id from arguments
        int replicaId = Integer.parseInt(args[0]);

        // get configurations from the config file
        Configuration config = new Configuration("jpaxos.properties");

        // create a new replica instance
        Replica replica = new Replica(config, replicaId, new FileStorageService(replicaId));

        // start replica
        replica.start();
    }
}
