package client;

import lombok.Getter;
import lsr.common.Configuration;
import lsr.paxos.client.Client;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;

/**
 * Initialize a JPaxos client for each HTTP session.
 */
@Getter
@Component
@Scope("session")
public class SessionScopedJPaxosClient {
    private Client client;

    @PostConstruct
    public void init() throws IOException {
        Configuration config = new Configuration("jpaxos.properties");
        this.client = new Client(config);
        this.client.connect();
    }

    @PreDestroy
    public void close() {
        if (client != null) {
            JPaxosClientCloser.closeClient(client);
        }
    }
}
