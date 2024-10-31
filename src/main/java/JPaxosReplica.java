import lsr.common.Configuration;
import lsr.paxos.replica.Replica;

public class JPaxosReplica {

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("Usage: java JPaxosReplica <replica_id>");
            System.exit(1);
        }

        int replicaId = Integer.parseInt(args[0]);
        Configuration config = new Configuration("jpaxos.properties");

        Replica replica = new Replica(config, replicaId, new FileStorageService(replicaId));
        replica.start();
    }
}
