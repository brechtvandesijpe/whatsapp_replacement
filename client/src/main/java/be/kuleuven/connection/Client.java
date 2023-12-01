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
    private final Map<Integer, Chat> chats;

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
        ConnectionHandler connectionHandler = new ConnectionHandler(bumpstring, chat, bulletinBoard, ui, this);
        connectionHandler.bump(bumpstring, passphrase);
        chat.add(connectionHandler);
        chats.put(bumpstring.hashCode(), chat);
    }

    public void bumpBack(String bumpstring, String passphrase) {
        Chat chat = new Chat(ui, bumpstring);
        ConnectionHandler connectionHandler = new ConnectionHandler(bumpstring, chat, bulletinBoard, ui, this);
        connectionHandler.bumpBack(bumpstring, passphrase);
        chat.add(connectionHandler);
        chats.put(bumpstring.hashCode(), chat);
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

    public void sendMessage(String text, int selectedContact) {
        Chat chat = chats.get(selectedContact);
        chat.sendMessage(new ChatMessage(username, text));
    }

    public void changeChatName(int chatIndex, String chatName) {
        Chat chat = chats.get(chatName.hashCode());
        chats.remove(chatName.hashCode());
        chat.setName(chatName);
        chats.put(chatIndex, chat);
    }

    public Object getChat(int selectedContact) {
        return chats.get(selectedContact);
    }

    public void addUserToChat(int selectedChat) {
//        String chatName = bumpstrings.get(selectedChat);
//        Chat chat = chats.get(chatName);
//        ConnectionHandler connectionHandler = new ConnectionHandler(chatName, chat, bulletinBoard, ui, this);
//        connectionHandler.startFetcher();
//        chat.add(connectionHandler);
//
//        // Let everyone in the group bump with you and bump with them too
//        chat.sendBump();
    }

    public boolean isChat(int selectedContact, Chat chat) {
        return chats.get(selectedContact) == chat;
    }

    public String getUsername() {
        return username;
    }
}
