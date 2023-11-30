package be.kuleuven.connection;

import be.kuleuven.UserInterface;
import be.kuleuven.model.Chat;
import be.kuleuven.model.ChatMessage;
import be.kuleuven.interfaces.BulletinBoardInterface;

import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.server.*;
import java.util.HashMap;
import java.util.Map;

public class Client extends UnicastRemoteObject {
    private final String username;
    private final UserInterface ui;
    private BulletinBoardInterface bulletinBoard;
    private final Map<String, Chat> chats;

    public Client(String username, UserInterface ui) throws RemoteException {
        super();
        this.username = username;
        this.ui = ui;
        this.chats = new HashMap<>();
    }

    public void join() {
        try {
            connectToRMIServer();
            ui.initiate(username);
        } catch(RemoteException e) {
            ui.showErrorDialog(e.getMessage());
            e.printStackTrace();
        }
    }

    public void bump(String bumpstring, String passphrase) {
        Chat chat = new Chat(ui, bumpstring);
        Connection connection = new Connection(bumpstring, chat, bulletinBoard, ui, this);
        connection.bump(bumpstring, passphrase);
        chat.add(connection);

        try {
            connection.sendMessage(true, new ChatMessage(username, username));
        } catch(Exception e) {
            e.printStackTrace();
        }

        connection.startFetcher();
        chats.put(bumpstring, chat);
    }

    public void bumpBack(String bumpstring, String passphrase) {
        Chat chat = new Chat(ui, bumpstring);
        Connection connection = new Connection(bumpstring, chat, bulletinBoard, ui, this);
        connection.bumpBack(bumpstring, passphrase);
        chat.add(connection);

        try {
            connection.sendMessage(true, new ChatMessage(username, username));
        } catch(Exception e) {
            e.printStackTrace();
        }

        connection.startFetcher();
        chats.put(bumpstring, chat);
    }

    public void leave() {
        try {
            bulletinBoard.leave(username);
        } catch(RemoteException ex) {
            throw new RuntimeException();
        }
    }

    public void connectToRMIServer() throws RemoteException {
        try {
            bulletinBoard = (BulletinBoardInterface) Naming.lookup("rmi://localhost/chat/");
            System.out.println("Verbonden met de RMI-server.\n");
        } catch (NotBoundException | MalformedURLException e) {
            System.err.println("Fout bij verbinden met de RMI-server:");
            e.printStackTrace();
        }
    }

    public void sendMessage(String text, String selectedContact) {
        System.out.println("Sending " + text + " to " + selectedContact);
        Chat chat = chats.get(selectedContact);
        chat.sendMessage(new ChatMessage(username, text));
    }

    public void changeChatName(String oldName, String newName) {
        Chat chat = chats.get(oldName);
        chat.setName(newName);
        chats.remove(oldName);
        chats.put(newName, chat);
    }

    public Object getChat(String selectedContact) {
        return chats.get(selectedContact);
    }
}
