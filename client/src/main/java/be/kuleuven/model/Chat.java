package be.kuleuven.model;

import java.util.ArrayList;
import be.kuleuven.connection.ConnectionHandler;
import be.kuleuven.UserInterface;

public class Chat extends ArrayList<ChatMessage> {
    private final ArrayList<ConnectionHandler> connectionHandlers;
    private final UserInterface ui;
    private String name;

    public Chat(UserInterface ui, String name) {
        super();
        connectionHandlers = new ArrayList<>();
        this.ui = ui;
        this.name = name;
    }

    public void add(ConnectionHandler connectionHandler) {
        synchronized(this) {
            connectionHandlers.add(connectionHandler);
        }
    }

    public void sendMessage(ChatMessage message) {
        for (ConnectionHandler connectionHandler : connectionHandlers) {
            try {
                connectionHandler.sendMessage(message);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean add(ChatMessage message) {
        synchronized(this) {
            boolean result = super.add(message);
            ui.update(this);
            return result;
        }
    }

    @Override
    public String toString() {
        synchronized(this) {
            StringBuilder sb = new StringBuilder();
            for (ChatMessage message : this) {
                sb.append(message).append("\n");
            }
            return sb.toString();
        }
    }

    public String getName() {
        return name;
    }

    public String[] sendBump() {
        // Array of bumpstring the new client has to bump with, you first bump with the client you add and then send this
        // array as the second message so he can add every single one of them and bump with them
        synchronized(this) {
            String[] result = new String[connectionHandlers.size()];

            for (ConnectionHandler connectionHandler : connectionHandlers) {
                result[connectionHandlers.indexOf(connectionHandler)] = connectionHandler.sendBump();
            }

            return result;
        }
    }

    public void setName(String chatName) {
        synchronized(this) {
            this.name = chatName;
        }
    }
}
