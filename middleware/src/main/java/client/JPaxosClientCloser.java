package client;

import lsr.paxos.client.Client;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class JPaxosClientCloser {

    /**
     * Use Java reflection to close JPaxos client.
     *
     * @param client JPaxos client.
     */
    public static void closeClient(Client client) {
        try {
            Method cleanCloseMethod = client.getClass().getDeclaredMethod("cleanClose");
            cleanCloseMethod.setAccessible(true);
            cleanCloseMethod.invoke(client);
            System.out.println("Client closed successfully.");
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
            System.out.println("Failed to close client: " + e.getMessage());
        }
    }
}
