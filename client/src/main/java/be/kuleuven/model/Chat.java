package be.kuleuven.model;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import be.kuleuven.connection.ConnectionHandler;
import be.kuleuven.UserInterface;
import org.json.JSONArray;
import org.json.JSONObject;
import be.kuleuven.connection.Client;

public class Chat {
    private static int count = 0;
    private ArrayList<ConnectionHandler> connectionHandlers;
    private ArrayList<ChatMessage> messages;
    private final UserInterface ui;
    private String name;
    private int id;

    public Chat(UserInterface ui, String name) {
        connectionHandlers = new ArrayList<>();
        messages = new ArrayList<>();
        this.ui = ui;
        this.name = name;
        this.id = count++;
    }

    public JSONObject toJSONObect() {
        synchronized(this) {
            JSONObject output = new JSONObject();
            output.put("id", this.id);
            output.put("name", this.name);

            JSONArray connectionHandlers = new JSONArray();
            for (ConnectionHandler connectionHandler : this.connectionHandlers) {
                connectionHandlers.put(connectionHandler.toJSONObject());
            }

            output.put("connectionHandlers", connectionHandlers);

            JSONArray messages = new JSONArray();
            for (ChatMessage message : this.messages) {
                messages.put(message.toJSONObject());
            }

            output.put("messages", messages);

            return output;
        }
    }

    public Chat(JSONObject data, UserInterface ui) {
        id = data.getInt("id");
        name = data.getString("name");
        this.ui = ui;

        connectionHandlers = new ArrayList<>();
        for (Object o : data.getJSONArray("connectionHandlers")) {
            JSONObject jsonObject = (JSONObject) o;
            connectionHandlers.add(new ConnectionHandler(jsonObject, this, Client.getBulletinBoard(), ui, Client.getInstance()));
        }

        messages = new ArrayList<>();

        try {
            for (Object o : data.getJSONArray("messages")) {
                JSONObject jsonObject = (JSONObject) o;
                messages.add(new ChatMessage(jsonObject));
            }
        } catch(Exception e) {}

        ui.update(this);
    }

    public void add(ConnectionHandler connectionHandler) {
        synchronized(this) {
            connectionHandlers.add(connectionHandler);
        }
    }

    public void add(ChatMessage message) {
        synchronized(this) {
            messages.add(message);
            ui.update(this);
        }
    }

    public void sendMessage(ChatMessage message) throws RemoteException {
        for (ConnectionHandler connectionHandler : connectionHandlers) {
            connectionHandler.sendMessage(message);
        }
    }

    public String getName() {
        synchronized(this) {
            return name;
        }
    }

    public void setName(String chatName) {
        synchronized(this) {
            this.name = chatName;
        }
    }

    @Override
    public String toString() {
        synchronized(this) {
            StringBuilder sb = new StringBuilder();
            for (ChatMessage message : messages) {
                sb.append(message).append("\n");
            }
            return sb.toString();
        }
    }

    public int getId() {
        return this.id;
    }
}
