package be.kuleuven.connection;

import javax.crypto.*;
import java.io.IOException;
import java.rmi.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.*;
import java.util.concurrent.*;


public class PeriodicMessageFetcher {
    // We could use some additive increase multiplicative decrease with a higher bound, instead of a fixed interval
    private static final int MESSAGE_FETCH_INTERVAL = 200;
    private final Client client;
    private final Timer timer = new Timer();

    // Constructor to initialize PeriodicMessageFetcher with a Client and UserInterface instance
    public PeriodicMessageFetcher(Client client) {
        this.client = client;
    }

    // Start fetching messages periodically
    public void start() {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

        Runnable task = () -> {
            try {
                int[] selected = client.getSelectedContacts();
                if(selected.length > 0) {
                    // Fetch messages IF there are selected contacts
                    String contactName = client.getUIContactAtIndex(selected[selected.length - 1]);
                    client.fetchMessages(contactName);
                }
            } catch (RemoteException | InvalidKeyException | BadPaddingException |
                     IllegalBlockSizeException | NoSuchAlgorithmException | InvalidKeySpecException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                System.err.println("NoSuchPaddingException");
                throw new RuntimeException(e);
            }
        };
        // Schedule the task at a fixed rate with the specified interval
        executorService.scheduleAtFixedRate(task, 0, MESSAGE_FETCH_INTERVAL, TimeUnit.MILLISECONDS);
    }

    public void stopTimer() {
        timer.cancel();
        System.err.println("Stopped PeriodicMessageFetcher");
    }

    public Client getClient() {
        return client;
    }

    public Timer getTimer() {
        return timer;
    }

    @Override
    public String toString() {
        return "PeriodicMessageFetcher{" +
                "timer=" + timer +
                ", client=" + client +
                '}';
    }

}