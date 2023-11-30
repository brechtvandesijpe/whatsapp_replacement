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
    private final Connection connection;
    private boolean stopWhenUsernameReceived;
    private final Timer timer = new Timer();

    // Constructor to initialize PeriodicMessageFetcher with a Client and UserInterface instance
    public PeriodicMessageFetcher(Connection connection) {
        this.connection = connection;
    }

    public void startNamefetch() {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

        Runnable task = new Runnable() {
            @Override
            public void run() {
                connection.fetchMessages(true); // Assuming connection is available
            }
        };

        // Schedule the task at a fixed rate with the specified interval
        executorService.scheduleAtFixedRate(task, 0, MESSAGE_FETCH_INTERVAL, TimeUnit.MILLISECONDS);
    }

    // Start fetching messages periodically
    public void start() {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

        Runnable task = new Runnable() {
            @Override
            public void run() {
                connection.fetchMessages(false); // Assuming connection is available
            }
        };

        // Schedule the task at a fixed rate with the specified interval
        executorService.scheduleAtFixedRate(task, 0, MESSAGE_FETCH_INTERVAL, TimeUnit.MILLISECONDS);
    }

    public void stopTimer() {
        timer.cancel();
        System.err.println("Stopped PeriodicMessageFetcher");
    }
}