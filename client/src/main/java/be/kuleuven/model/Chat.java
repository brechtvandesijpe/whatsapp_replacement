package be.kuleuven.model;

import java.util.ArrayList;
import be.kuleuven.connection.Connection;
import be.kuleuven.UserInterface;

public class Chat extends ArrayList<ChatMessage> {
    private final ArrayList<Connection> connections;
    private final UserInterface ui;
    private String name;

    public Chat(UserInterface ui, String name) {
        super();
        connections = new ArrayList<>();
        this.ui = ui;
        this.name = name;
    }

    public void add(Connection connection) {
        connections.add(connection);
    }

    public void sendMessage(ChatMessage message) {
        for (Connection connection : connections) {
            System.out.println("Sending message to " + connection.getName());
            try {
                connection.sendMessage(false, message);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean add(ChatMessage message) {
        boolean result = super.add(message);
        ui.update(this);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (ChatMessage message : this) {
            sb.append(message).append("\n");
        }
        return sb.toString();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] sendBump() {
        // Array of bumpstring the new client has to bump with, you first bump with the client you add and then send this
        // array as the second message so he can add every single one of them and bump with them
        String[] result = new String[connections.size()];

        for (Connection connection : connections) {
            result[connections.indexOf(connection)] = connection.sendBump();
        }

        return result;
    }
}
