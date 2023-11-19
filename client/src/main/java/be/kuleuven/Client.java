package be.kuleuven;

import be.kuleuven.Instances.*;
import be.kuleuven.Interfaces.*;
import be.kuleuven.Managers.*;
import be.kuleuven.MessageHandling.*;

import javax.crypto.*;
import java.net.*;
import java.rmi.*;
import java.rmi.server.*;
import java.security.*;
import java.security.spec.*;
import java.util.*;

// Class representing the main client application
public class Client extends UnicastRemoteObject {

    // Remote interface for communication with the bulletin board
    public BulletinBoardInterface bulletinBoardInterface;
    private final UserInterface userInterface;
    private final String clientName;
    private final List<Entry> entries_AB;
    private final List<Entry> entries_BA;
    private final MessageHandler messageHandler;
    private final HistoryManager historyManager;

    // Client initialization
    public Client(String clientName, UserInterface userInterface, BulletinBoardInterface bulletinBoardImpl) throws RemoteException {
        super(); // UnicastRemoteObject
        this.clientName = clientName;
        this.userInterface = userInterface;
        this.entries_AB = new ArrayList<>();
        this.entries_BA = new ArrayList<>();
        this.bulletinBoardInterface = bulletinBoardImpl;
        this.messageHandler = new MessageHandler(this);
        this.historyManager = new HistoryManager();
    }

    // Method to add a contact to the client's entry lists
    public void addContact(ContactInfo contactInfo) {
        historyManager.initializeContactHistory(contactInfo.getContactName());
        entries_AB.add(new Entry(contactInfo.getContactName(), new BulletinEntry(contactInfo.getBoxNumber_AB(), contactInfo.getTag_AB(), contactInfo.getSecretKey_AB())));
        entries_BA.add(new Entry(contactInfo.getContactName(), new BulletinEntry(contactInfo.getBoxNumber_BA(), contactInfo.getTag_BA(), contactInfo.getSecretKey_BA())));
    }


    public void connectToRMIServer() {
        try {
            bulletinBoardInterface = (BulletinBoardInterface) Naming.lookup("rmi://localhost/chat/");
            System.out.println("Verbonden met de RMI-server.\n");
        } catch (RemoteException | NotBoundException | MalformedURLException e) {
            System.err.println("Fout bij verbinden met de RMI-server:");
            e.printStackTrace();
        }
    }

    // Method to get the BulletinEntry for a contact from entries_AB
    public BulletinEntry getBulletEntry_AB_from(String name) {
        return entries_AB.stream()
                .filter(entry -> entry.getName().equals(name))
                .findFirst()
                .map(Entry::getBulletinEntry)
                .orElse(null);
    }

    // Method to get the BulletinEntry for a contact from entries_BA
    public BulletinEntry getBulletinEntry_BA_from(String name) {
        return entries_BA.stream()
                .filter(entry -> entry.getName().equals(name))
                .findFirst()
                .map(Entry::getBulletinEntry)
                .orElse(null);
    }

    // Method to fetch messages from the bulletin board for a specific contact
    public void getMessagesFrom(String contactName) throws IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, RemoteException {
        messageHandler.getMessagesFrom(contactName);
    }

    // Method to send a message to a specific contact
    public void sendMessageTo(String contactName, String message) throws IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, RemoteException, InvalidKeyException {
        messageHandler.sendMessage(contactName, message);
    }

    // Method to transform a message before sending it
    public String transformMessage(String contactName, String message) throws RemoteException {
        // Generate a random tag
        String randomTag = generateRandomTag();

        // Get a random mailbox number
        int newBoxNumber = getRandomMailboxNumber();

        // Update the BulletinEntry for the given friend
        BulletinEntry bulletinEntry_AB = getBulletEntry_AB_from(contactName);
        bulletinEntry_AB.setTag(randomTag.getBytes());
        bulletinEntry_AB.setBoxNumber(newBoxNumber);

        // Build the final message format
        return randomTag + String.format("%02d", newBoxNumber) + message;
    }

    private String generateRandomTag() {
        StringBuilder tagBuilder = new StringBuilder();
        for (int i = 0; i < 256; i++) {
            tagBuilder.append(getRandomString());
        }
        return tagBuilder.toString();
    }

    private int getRandomMailboxNumber() throws RemoteException {
        return (int) (Math.random() * this.bulletinBoardInterface.getAmountOfMailboxes());
    }

    private static char getRandomString() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_+=<>?";

        // Kies willekeurig een teken uit de lijst
        int rnd = (int) (Math.random() * characters.length());

        return characters.charAt(rnd);
    }

    public UserInterface getUserInterface() {
        return userInterface;
    }

    public BulletinBoardInterface getBulletinBoardInterface() {
        return bulletinBoardInterface;
    }

    public String getName() {
        return clientName;
    }

    public String getClientName() {
        return clientName;
    }

    public List<Entry> getEntries_AB() {
        return entries_AB;
    }

    public List<Entry> getEntries_BA() {
        return entries_BA;
    }

    public MessageHandler getMessageHandler() {
        return messageHandler;
    }

    public HistoryManager getHistoryManager() {
        return historyManager;
    }
}
