package be.kuleuven.model;

public class ChatMessage {
    private String username;
    private String message;

    public ChatMessage(String username, String message) {
        this.username = username;
        this.message = message;
    }

    @Override
    public String toString() {
        return username + ": " + message;
    }
}
