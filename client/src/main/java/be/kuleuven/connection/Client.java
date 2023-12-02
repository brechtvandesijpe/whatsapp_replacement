package be.kuleuven.connection;

import be.kuleuven.UserInterface;
import be.kuleuven.model.Chat;
import be.kuleuven.model.ChatMessage;
import be.kuleuven.interfaces.BulletinBoardInterface;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.server.*;
import java.util.HashMap;
import java.util.Map;

public class Client extends UnicastRemoteObject {
    private String username;
    private final UserInterface ui;
    private static BulletinBoardInterface bulletinBoard;
    private static Client instance;
    private Map<Integer, Chat> chats;

    private Client(String username, UserInterface ui) throws RemoteException {
        super();
        this.username = username;
        this.ui = ui;
        this.chats = new HashMap<>();
    }

    public static BulletinBoardInterface getBulletinBoard() {
        return bulletinBoard;
    }

    public static Client createInstance(String username, UserInterface ui) throws RemoteException {
        instance = new Client(username, ui);
        return instance;
    }

    public static Client getInstance() {
        return instance;
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
        saveState();
    }

    public void bumpBack(String bumpstring, String passphrase) {
        Chat chat = new Chat(ui, bumpstring);
        ConnectionHandler connectionHandler = new ConnectionHandler(bumpstring, chat, bulletinBoard, ui, this);
        connectionHandler.bumpBack(bumpstring, passphrase);
        chat.add(connectionHandler);
        chats.put(bumpstring.hashCode(), chat);
        saveState();
    }

    public void leave() {
        try {
            chats.remove(ui.getSelectedContact());
            ui.removeSelectedContact();
            bulletinBoard.leave(username);
        } catch(RemoteException ex) {
            throw new RuntimeException();
        }
    }

    public static void connectToRMIServer() throws RemoteException {
        try {
            bulletinBoard = (BulletinBoardInterface) Naming.lookup("rmi://localhost/chat/");
            System.out.println("Verbonden met de RMI-server.\n");
        } catch (NotBoundException | MalformedURLException e) {
            System.err.println("Fout bij verbinden met de RMI-server:");
            e.printStackTrace();
        }
    }

    public void sendMessage(String text, int selectedContact) {
        System.out.println("got chat from " + selectedContact);
        Chat chat = chats.get(selectedContact);
        chat.sendMessage(new ChatMessage(username, text));
        saveState();
    }

    public void changeChatName(int chatIndex, String chatName) {
        Chat chat = chats.get(chatName.hashCode());
        chats.remove(chatName.hashCode());
        chat.setName(chatName);
        chats.put(chatIndex, chat);
        saveState();
    }

    public Chat getChat(int selectedContact) {
        return chats.get(selectedContact);
    }

    public boolean isChat(int selectedContact, Chat chat) {
        return chats.get(selectedContact) == chat;
    }

    public String getUsername() {
        return username;
    }

    public JSONObject toJSONObject() {
        JSONObject output = new JSONObject();
        output.put("username", username);

        JSONArray chats = new JSONArray();
        System.out.println(this.chats);
        for (Integer key : this.chats.keySet()) {
            JSONObject object = new JSONObject();
            object.put("key", key);
            object.put("chat", this.chats.get(key).toJSONObect());
            chats.put(object);
        }
        output.put("chats", chats);

        JSONArray contacts = ui.getJSONContacts();
        output.put("contacts", contacts);

        return output;
    }

    public void loadJSONData(JSONObject data) {
        username = data.getString("username");
        ui.setContactListModel(data.getJSONArray("contacts"));

        chats = new HashMap<>();
        for (Object o : data.getJSONArray("chats")) {
            JSONObject jsonObject = (JSONObject) o;
            chats.put(jsonObject.getInt("key"), new Chat(jsonObject.getJSONObject("chat"), ui));
        }
    }

    public void saveState() {
        JSONObject data = toJSONObject();
        data.put("contacts", ui.getJSONContacts());
        try (FileWriter fileWriter = new FileWriter("client_" + username + ".json")) {
            fileWriter.write(data.toString());
            System.out.println("Current state written to " + "client_" + username + ".json");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void restore() {
        chats = null;
        try (FileReader fileReader = new FileReader("client_" + username + ".json")) {
            JSONTokener tokener = new JSONTokener(fileReader);
            loadJSONData(new JSONObject(tokener));
            System.out.println("Restored from client_" + username + ".json");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
