package be.kuleuven.connection;

import be.kuleuven.UserInterface;
import be.kuleuven.model.Chat;
import be.kuleuven.model.ChatMessage;
import be.kuleuven.interfaces.BulletinBoardInterface;

public class ConnectionHandler {
    private final Chat chat;
    private final BulletinBoardInterface bulletinBoard;
    private String name;
    private DataConnection dataConnection;
    private MetaConnection metaConnection;

    public ConnectionHandler(String name, Chat chat, BulletinBoardInterface bulletinBoard, UserInterface ui, Client client) {
        this.name = name;
        this.chat = chat;
        this.bulletinBoard = bulletinBoard;
        metaConnection = new MetaConnection(bulletinBoard, chat, ui, client, this);
    }

    public String getName() {
        return name;
    }

    public void sendMessage(ChatMessage message) {
        dataConnection.sendMessage(message);
    }

    public void bump(String bumpstring, String passphrase) {
        metaConnection.bump(bumpstring, passphrase);
    }

    public void bumpBack(String bumpstring, String passphrase) {
        metaConnection.bumpBack(bumpstring, passphrase);
    }

    public String sendBump() {
        String bumpstring = RandomStringGenerator.generateRandomString(10);
        // TODO: send the bumpstring in the group, let them bump with it and make sure they add it to the same chat as connection
        return bumpstring;
    }

    public void setName(String name) {
        this.name = name;
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
}
