package be.kuleuven.MessageHandling;

import be.kuleuven.*;

import javax.crypto.*;
import java.io.IOException;
import java.rmi.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.*;
import java.util.concurrent.*;


public class PeriodicMessageFetcher {
    private static final int MESSAGE_FETCH_INTERVAL = 200;
    private final Client client;
    private final UserInterface userInterface;
    private final Timer timer = new Timer();

    public PeriodicMessageFetcher(Client client, UserInterface userInterface) {
        this.client = client;
        this.userInterface = userInterface;
    }

    public void startPeriodicMessageFetching() {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

        Runnable task = () -> {
            try {
                int[] selected = client.getUserInterface().getSelectedContacts();
                if(selected.length > 0) {
                    // Fetch messages IF there are selected contacts
                    client.getMessagesFrom(client.getUserInterface().getContactAtIndex(selected[selected.length - 1]));
                    userInterface.saveState();
                }
            } catch (RemoteException | InvalidKeyException | BadPaddingException |
                     IllegalBlockSizeException | NoSuchAlgorithmException | InvalidKeySpecException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                System.err.println("NoSuchPaddingException");
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
        // Schedule the task at a fixed rate with the specified interval
        executorService.scheduleAtFixedRate(task, 0, MESSAGE_FETCH_INTERVAL, TimeUnit.MILLISECONDS);
    }

    public void stopTimer() {
        timer.cancel();
        System.err.println("Stopped PeriodicMessageFetcher of " + client.getName());
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
