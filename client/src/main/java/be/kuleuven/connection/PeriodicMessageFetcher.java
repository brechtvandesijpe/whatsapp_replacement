package be.kuleuven.connection;

import java.util.concurrent.*;


public class PeriodicMessageFetcher {
    // We could use some additive increase multiplicative decrease with a higher bound, instead of a fixed interval
    private static final int MESSAGE_FETCH_INTERVAL = 200;
    private final Connection connection;

    // Constructor to initialize PeriodicMessageFetcher with a Client and UserInterface instance
    public PeriodicMessageFetcher(Connection connection) {
        this.connection = connection;
    }

    // Start fetching messages periodically
    public void start() {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

        // Assuming connection is available
        Runnable task = connection::fetchMessages;

        // Schedule the task at a fixed rate with the specified interval
        executorService.scheduleAtFixedRate(task, 0, MESSAGE_FETCH_INTERVAL, TimeUnit.MILLISECONDS);
    }
}