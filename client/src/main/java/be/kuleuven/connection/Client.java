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
    private static final int TAG_SUBSTRING_START = 0;
    private static final int TAG_SUBSTRING_END = 256;
    private static final int BOX_NUMBER_SUBSTRING_START = 256;
    private static final int BOX_NUMBER_SUBSTRING_END = 258;
    private static final String HASH_ALGORITHM = "SHA-256";
    private String username;
    private UserInterface ui;
    private BulletinBoardInterface bulletinBoard;
    private PeriodicMessageFetcher fetcher;
    private Map<String, Chat> chats;

    public Client(String username, UserInterface ui) throws RemoteException {
        super();
        this.username = username;
        this.ui = ui;
        this.chats = new HashMap<>();
    }

    public String getUsername() {
        return username;
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
        Chat chat = new Chat(ui, "bump back needed");
        Connection connection = new Connection("bump back needed", chat, ui, bulletinBoard);
        connection.bump(bumpstring, passphrase);
        chat.add(connection);

        try {
            connection.sendMessage(new ChatMessage(username, username));
        } catch(Exception e) {
            e.printStackTrace();
        }

        connection.startNameFetch();
        String name = connection.getName();
        chat.setName(name);
        chats.put(name, chat);
    }

    public void bumpBack(String bumpstring, String passphrase) {
        Chat chat = new Chat(ui, "bump back needed");
        Connection connection = new Connection("bump back needed", chat, ui, bulletinBoard);
        connection.bumpBack(bumpstring, passphrase);
        chat.add(connection);

        try {
            connection.sendMessage(new ChatMessage(username, username));
        } catch(Exception e) {
            e.printStackTrace();
        }

        connection.startNameFetch();
        String name = connection.getName();
        chat.setName(name);
        chats.put(name, chat);
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
        } catch (NotBoundException | MalformedURLException | RemoteException e) {
            System.err.println("Fout bij verbinden met de RMI-server:");
            e.printStackTrace();
        }
    }

    public void sendMessage(String text, String selectedContact) {
        Chat chat = chats.get(selectedContact);
        ChatMessage message = new ChatMessage(username, text);
        chat.sendMessage(message);
        chat.add(message);
    }
}
