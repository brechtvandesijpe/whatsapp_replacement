package be.kuleuven;

import be.kuleuven.Instances.*;
import be.kuleuven.Interfaces.*;

import java.net.*;
import java.rmi.*;
import java.util.*;

public class Client {

    public BulletinBoardInterface bulletinBoardInterface;
    private final UserInterface userInterface;
    private String clientName;
    private final List<Entry> entries_AB;
    private final List<Entry> entries_BA;

    public Client(String clientName, UserInterface userInterface){
        this.clientName = clientName;
        this.userInterface = userInterface;
        this.entries_AB = new ArrayList<>();
        this.entries_BA = new ArrayList<>();
    }

    public void connectToRMIServer() throws RemoteException {
        try {
            bulletinBoardInterface = (BulletinBoardInterface) Naming.lookup("rmi://localhost/chat/");
            System.out.println("Verbonden met de RMI-server.\n");
        } catch (RemoteException | NotBoundException | MalformedURLException e) {
            System.err.println("Fout bij verbinden met de RMI-server:");
            e.printStackTrace();
        }
    }

    public BulletinEntry getBulletEntry_AB_from(String name) {
        return entries_AB.stream()
                .filter(entry -> entry.getName().equals(name))
                .findFirst()
                .map(Entry::getBulletinEntry)
                .orElse(null);
    }

    public BulletinEntry getBulletinEntry_BA_from(String name) {
        return entries_BA.stream()
                .filter(entry -> entry.getName().equals(name))
                .findFirst()
                .map(Entry::getBulletinEntry)
                .orElse(null);
    }

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

}
