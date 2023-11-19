package be.kuleuven.Managers;

import java.util.*;

public class HistoryManager {
    // Key: contactName,
    // Value: messages
    private final Map<String, List<String>> messageHistory;

    public HistoryManager() {
        this.messageHistory = new HashMap<>();
    }

    public void initializeContactHistory(String contactName) {
        messageHistory.put(contactName, new ArrayList<>());
    }

    public Map<String, List<String>> getMessageHistory() {
        return messageHistory;
    }

    @Override
    public String toString() {
        return "HistoryManager{" +
                "messageHistory=" + messageHistory +
                '}';
    }
}
