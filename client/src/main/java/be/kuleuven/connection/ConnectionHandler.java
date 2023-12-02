package be.kuleuven.connection;

import be.kuleuven.UserInterface;
import be.kuleuven.model.Chat;
import be.kuleuven.model.ChatMessage;
import be.kuleuven.interfaces.BulletinBoardInterface;
import org.json.JSONObject;

import java.rmi.RemoteException;

public class ConnectionHandler {
    private static int count = 0;
    private Chat chat;
    private BulletinBoardInterface bulletinBoard;
    private String name;
    private DataConnection dataConnection;
    private MetaConnection metaConnection;
    private int id;

    public ConnectionHandler(String name, Chat chat, BulletinBoardInterface bulletinBoard, UserInterface ui, Client client) {
        this.name = name;
        this.chat = chat;
        this.bulletinBoard = bulletinBoard;
        metaConnection = new MetaConnection(bulletinBoard, ui, client, this);
        id = count++;
    }

    public JSONObject toJSONObject() {
        JSONObject output = new JSONObject();

        output.put("name", name);
        output.put("id", id);
        try {
            output.put("dataConnection", dataConnection.toJSONObject());
        } catch(NullPointerException e) {}
        output.put("metaConnection", metaConnection.toJSONObject());

        return output;
    }

    public ConnectionHandler(JSONObject data, Chat chat, BulletinBoardInterface bulletinBoard, UserInterface ui, Client client) {
        name = data.getString("name");
        id = data.getInt("id");

        metaConnection = new MetaConnection(data.getJSONObject("metaConnection"), bulletinBoard, ui, client, this);
        metaConnection.startFetcher();

        if (metaConnection.isConfirmed()) {
            dataConnection = new DataConnection(data.getJSONObject("dataConnection"), bulletinBoard, chat);
            dataConnection.startFetcher();
        }

        this.chat = chat;
        this.bulletinBoard = bulletinBoard;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }

    public void sendMessage(ChatMessage message) throws RemoteException {
        dataConnection.sendMessage(message);
    }

    public void bump(String bumpstring, String passphrase) throws RemoteException {
        metaConnection.bump(bumpstring, passphrase);
    }

    public void bumpBack(String bumpstring, String passphrase) throws RemoteException {
        metaConnection.bumpBack(bumpstring, passphrase);
    }

    public void startDataConnection(ConnectionInfo ab, ConnectionInfo ba) {
        try {
            dataConnection.stopFetcher();
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }

        dataConnection = new DataConnection(ab, ba, bulletinBoard, chat, name);
        dataConnection.startFetcher();
    }

    public int getId() {
        return id;
    }
}
