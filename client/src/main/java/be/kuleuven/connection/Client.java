package be.kuleuven.connection;

import be.kuleuven.UserInterface;
import be.kuleuven.model.Chat;
import be.kuleuven.model.ChatMessage;
import be.kuleuven.model.Contact;
import be.kuleuven.interfaces.BulletinBoardInterface;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.swing.*;
import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.server.*;
import java.security.NoSuchAlgorithmException;
import java.security.InvalidKeyException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;
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
    private Map<String, Contact> contacts;
    private Map<String, Chat> chats;
    private Map<String, BulletinEntry> bulletinEntries_AB;
    private Map<String, BulletinEntry> bulletinEntries_BA;
    private PeriodicMessageFetcher fetcher;

    public Client(String username, UserInterface ui) throws RemoteException {
        super();
        this.username = username;
        this.ui = ui;
        this.contacts = new HashMap<>();
        this.chats = new HashMap<>();
        this.bulletinEntries_AB = new HashMap<>();
        this.bulletinEntries_BA = new HashMap<>();
    }

    public String getUsername() {
        return username;
    }

    public void join() {
        try {
            connectToRMIServer();
            fetcher = new PeriodicMessageFetcher(this);
            fetcher.start();
            ui.initiate(username);
        } catch(RemoteException e) {
            ui.showErrorDialog(e.getMessage());
            e.printStackTrace();
        }
    }

    public void bump() {
        String bumpstring = RandomStringGenerator.generateRandomString(10);

        JTextField passphraseField = new JTextField(20);
        passphraseField.setText(bumpstring);
        passphraseField.setEditable(false);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JLabel("Your bumpstring is:"));
        panel.add(passphraseField);
        panel.add(new JLabel("Please enter the passphrase of your choice: "));

        String passphrase = "";
        while (passphrase.isEmpty()) {
            passphrase = JOptionPane.showInputDialog(null, panel, "Bump", JOptionPane.PLAIN_MESSAGE);
            if (passphrase.isEmpty()) {
                ui.showErrorDialog("Your passphrase cannot be empty!");
            }
        }

        int boxNumber_AB;
        int boxNumber_BA;

        try {
            boxNumber_AB = Math.abs(RandomStringGenerator.deriveIntFromPasshrase(passphrase)) % bulletinBoard.getAmountOfMailboxes();
            boxNumber_BA = Math.abs(RandomStringGenerator.deriveIntFromPasshrase(new StringBuilder(passphrase).reverse().toString())) % bulletinBoard.getAmountOfMailboxes();
            System.out.println("Boxnumber AB: " + boxNumber_AB + ", BoxNumber BA: " + boxNumber_BA);
        } catch(RemoteException ex) {
            throw new RuntimeException();
        }

        byte[] tag_AB = RandomStringGenerator.deriveBytesFromPassphrase(passphrase);
        byte[] tag_BA = RandomStringGenerator.deriveBytesFromPassphrase(new StringBuilder(passphrase).reverse().toString());

        assert tag_AB != null;
        assert tag_BA != null;

        System.out.println("tag AB: " + new String(tag_AB) + ", tag BA: " + new String(tag_BA));

        System.out.println("Passphrase: " + passphrase);
        SecretKey secretKey_AB = SecurityManager.getSymmetricKey(passphrase, tag_AB);
        SecretKey secretKey_BA = SecurityManager.getSymmetricKey(passphrase, tag_BA);

        String contactName = "<unnamed (bump back needed!)>";
        contacts.put(contactName, new Contact(contactName, boxNumber_AB, boxNumber_BA, tag_AB, tag_BA, secretKey_AB, secretKey_BA));
        bulletinEntries_AB.put(contactName, new BulletinEntry(boxNumber_AB, tag_AB, secretKey_AB));
        bulletinEntries_BA.put(contactName, new BulletinEntry(boxNumber_BA, tag_BA, secretKey_BA));
        ui.addContact(contactName);
        chats.put(contactName, new Chat());

        // TODO: send your own username in the first message & wait for the other's username
        try {
            sendMessage(contactName, username);
        } catch(Exception e) {
            e.printStackTrace();
        }

        ChatMessage message = null;
        while (message == null) {
            try {

            } catch(Exception e) {}
        }
    }

    public void bumpBack() {
        String bumpstring = "";
        while (bumpstring.isEmpty()) {
            bumpstring = JOptionPane.showInputDialog("Please enter the bumpstring: ");
            if (bumpstring.isEmpty()) {
                ui.showErrorDialog("Your passphrase cannot be empty!");
            }
        }

        String passphrase = "";
        while (passphrase.isEmpty()) {
            passphrase = JOptionPane.showInputDialog("Please enter the passphrase: ");
            if (passphrase.isEmpty()) {
                ui.showErrorDialog("Your passphrase cannot be empty!");
            }
        }

        int boxNumber_AB;
        int boxNumber_BA;

        try {
            boxNumber_BA = Math.abs(RandomStringGenerator.deriveIntFromPasshrase(passphrase)) % bulletinBoard.getAmountOfMailboxes();
            boxNumber_AB = Math.abs(RandomStringGenerator.deriveIntFromPasshrase(new StringBuilder(passphrase).reverse().toString())) % bulletinBoard.getAmountOfMailboxes();
            System.out.println("AB: " + boxNumber_AB + ", BA: " + boxNumber_BA);
        } catch(RemoteException ex) {
            throw new RuntimeException();
        }

        byte[] tag_BA = RandomStringGenerator.deriveBytesFromPassphrase(passphrase);
        byte[] tag_AB = RandomStringGenerator.deriveBytesFromPassphrase(new StringBuilder(passphrase).reverse().toString());

        assert tag_AB != null;
        assert tag_BA != null;

        System.out.println("tag_AB: " + new String(tag_AB) + ", " + new String(tag_BA));

        System.out.println("Passphrase: " + passphrase);
        SecretKey secretKey_AB = SecurityManager.getSymmetricKey(passphrase, tag_AB);
        SecretKey secretKey_BA = SecurityManager.getSymmetricKey(passphrase, tag_BA);

        String contactName = "<unnamed (bump back needed!)>";
        contacts.put(contactName, new Contact(contactName, boxNumber_AB, boxNumber_BA, tag_AB, tag_BA, secretKey_AB, secretKey_BA));
        bulletinEntries_AB.put(contactName, new BulletinEntry(boxNumber_AB, tag_AB, secretKey_AB));
        bulletinEntries_BA.put(contactName, new BulletinEntry(boxNumber_BA, tag_BA, secretKey_BA));
        ui.addContact(contactName);
        chats.put(contactName, new Chat());

        // TODO: read in the contactname and put your own in the new message
        try {
            sendMessage(contactName, username);
        } catch(Exception e) {
            e.printStackTrace();
        }

        ChatMessage message = null;
        while (message == null) {
            try {

            } catch(Exception e) {}
        }
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

    public ChatMessage sendMessage(String contactName, String message) throws NoSuchAlgorithmException, NoSuchPaddingException,
            IllegalBlockSizeException, BadPaddingException, InvalidKeyException, RemoteException {

        if (!message.isEmpty()) {
            BulletinEntry bulletinEntry_AB = bulletinEntries_AB.get(contactName);
            System.out.println(contactName);

            int boxNumber_AB = bulletinEntry_AB.getBoxNumber();
            byte[] tag_AB = Arrays.copyOf(bulletinEntry_AB.getTag(), bulletinEntry_AB.getTag().length);
            String transformedMessage = transformMessage(contactName, message);
            System.out.println("TransformedMessage: " + transformedMessage);
            System.out.println("Sender: " + Arrays.toString(tag_AB) + ", " + boxNumber_AB);
            byte[] hashedMessage = MessageHandler.encryptMessage(transformedMessage.getBytes(), bulletinEntry_AB.getSecretKey());
            byte[] hashedTag = MessageHandler.hashTag(tag_AB);

            MessageHandler.deriveAndUpdateSecretKey(bulletinEntry_AB);
            bulletinBoard.postMessage(boxNumber_AB, hashedMessage, hashedTag);
        }

        return new ChatMessage(username, message);
    }

    public void addMessage(String contactName, ChatMessage message) {
        chats.get(contactName).add(message);
    }

    public void postMessage(int boxNumber, byte[] message, byte[] hashedTag) {
        try {
            bulletinBoard.postMessage(boxNumber, message, hashedTag);
        } catch(RemoteException ex) {
            throw new RuntimeException();
        }
    }

    private static char getRandomString() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_+=<>?";

        // Kies willekeurig een teken uit de lijst
        int rnd = (int) (Math.random() * characters.length());

        return characters.charAt(rnd);
    }

    private String generateRandomTag() {
        StringBuilder tagBuilder = new StringBuilder();
        for (int i = 0; i < 256; i++) {
            tagBuilder.append(getRandomString());
        }
        return tagBuilder.toString();
    }

    private int getRandomMailboxNumber() throws RemoteException {
        return (int) (Math.random() * this.bulletinBoard.getAmountOfMailboxes());
    }

    public String transformMessage(String contactName, String message) throws RemoteException {
        // Generate a random tag
        String randomTag = generateRandomTag();

        // Get a random mailbox number
        int newBoxNumber = getRandomMailboxNumber();

        // Update the BulletinEntry for the given friend
        BulletinEntry bulletinEntry_AB = bulletinEntries_AB.get(contactName);
        bulletinEntry_AB.setTag(randomTag.getBytes());
        bulletinEntry_AB.setBoxNumber(newBoxNumber);

        // Build the final message format
        return randomTag + String.format("%02d", newBoxNumber) + message;
    }

    public int[] getSelectedContacts() {
        return ui.getSelectedContacts();
    }

    public Object getContactAtIndex(int i) {
        return ui.getContactAtIndex(i);
    }

    public void fetchMessages(String contactName) throws NoSuchAlgorithmException, RemoteException, IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, InvalidKeySpecException, InvalidKeyException {
        BulletinEntry bulletinEntry_BA = bulletinEntries_BA.get(contactName);
        byte[] currentMessage  = bulletinBoard.getMessage(bulletinEntry_BA.getBoxNumber(), bulletinEntry_BA.getTag());

        while (currentMessage != null) {
            System.out.println("Proberen decrypteren met key: " + bulletinEntry_BA.getSecretKey());
            String newMessage = new String(SecurityManager.decryptMessage(currentMessage, bulletinEntry_BA.getSecretKey()));

            // Extraheren van de tag van index 0 tot 32 (32 bytes)
            byte[] tag_BA = newMessage.substring(TAG_SUBSTRING_START, TAG_SUBSTRING_END).getBytes();
            int boxNumber_BA = Integer.parseInt(newMessage.substring(BOX_NUMBER_SUBSTRING_START, BOX_NUMBER_SUBSTRING_END));
            String message = newMessage.substring(BOX_NUMBER_SUBSTRING_END);

            bulletinEntry_BA.setTag(tag_BA);
            bulletinEntry_BA.setBoxNumber(boxNumber_BA);
            System.out.println("Receiver: " + Arrays.toString(tag_BA) + ", " + boxNumber_BA);
            bulletinEntry_BA.setSecretKey(SecurityManager.getSymmetricKey(Base64.getEncoder().encodeToString(bulletinEntry_BA.getSecretKey().getEncoded()), MessageHandler.deriveSalt(tag_BA)));

            ChatMessage chatMessage = new ChatMessage(contactName, message);
            ui.appendChatArea(chatMessage);
            chats.get(contactName).add(chatMessage);
            System.out.println("{name: " + contactName + ", boxNumber: " + bulletinEntry_BA.getBoxNumber() + "}");

            currentMessage  = bulletinBoard.getMessage(bulletinEntry_BA.getBoxNumber(), bulletinEntry_BA.getTag());
        }
    }

    public String getUIContactAtIndex(int i) {
        return ui.getContactAtIndex(i);
    }
}
