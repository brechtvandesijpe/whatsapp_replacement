package be.kuleuven.connection;

import be.kuleuven.UserInterface;
import be.kuleuven.model.Chat;
import be.kuleuven.model.ChatMessage;
import be.kuleuven.interfaces.BulletinBoardInterface;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.SecretKey;

public class Connection {
    private static final int TAG_SUBSTRING_START = 0;
    private static final int TAG_SUBSTRING_END = 256;
    private static final int BOX_NUMBER_SUBSTRING_START = 256;
    private static final int BOX_NUMBER_SUBSTRING_END = 258;
    private static final String HASH_ALGORITHM = "SHA-256";
    private final Chat chat;
    private BulletinBoardInterface bulletinBoard;
    private String name;
    private ConnectionInfo ab;
    private ConnectionInfo ba;
    private final PeriodicMessageFetcher fetcher;
    private UserInterface ui;

    public Connection(String name, Chat chat, UserInterface ui, BulletinBoardInterface bulletinBoard) {
        this.chat = chat;
        this.bulletinBoard = bulletinBoard;
        this.name = name;
        this.fetcher = new PeriodicMessageFetcher(this);
        this.ui = ui;
    }

    private static char getRandomChar() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_+=<>?";

        // Kies willekeurig een teken uit de lijst
        int rnd = (int) (Math.random() * characters.length());

        return characters.charAt(rnd);
    }

    private static String generateRandomTag() {
        StringBuilder tagBuilder = new StringBuilder();
        for (int i = 0; i < 256; i++) {
            tagBuilder.append(getRandomChar());
        }
        return tagBuilder.toString();
    }

    private void setName(String name) {
        this.name = name;
        ui.addContact(name);
    }

    public String getName() {
        return name;
    }

    public String transformMessage(String message) {
        // Generate a random tag
        String randomTag = generateRandomTag();

        int newBoxNumber = 0;
        try {
            newBoxNumber = (int) (Math.random() * bulletinBoard.getAmountOfMailboxes());
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }

        // Update the BulletinEntry for the given friend
        ab.setTag(randomTag.getBytes());
        ab.setBoxNumber(newBoxNumber);

        // Build the final message format
        return randomTag + String.format("%02d", newBoxNumber) + message;
    }

    public void sendMessage(ChatMessage message) {
        chat.add(message);

        int boxNumber_AB = ab.getBoxNumber();
        byte[] tag_AB = Arrays.copyOf(ab.getTag(), ab.getTag().length);
        String transformedMessage = transformMessage(message.getMessage());
        System.out.println("TransformedMessage: " + transformedMessage);
        System.out.println("Sender: " + Arrays.toString(tag_AB) + ", " + boxNumber_AB);
        byte[] hashedMessage = new byte[0];
        try {
            hashedMessage = MessageHandler.encryptMessage(transformedMessage.getBytes(), ab.getSecretKey());
        } catch (NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException |
                 NoSuchAlgorithmException e) {


        }
        byte[] hashedTag
                ;
        try {
            hashedTag = MessageHandler.hashTag(tag_AB);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        MessageHandler.deriveAndUpdateSecretKey(ab);
        try {
            bulletinBoard.postMessage(boxNumber_AB, hashedMessage, hashedTag);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }

        try {
            bulletinBoard.postMessage(ab.getBoxNumber(), hashedMessage, hashedTag);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] hashTag(byte[] tag) throws NoSuchAlgorithmException {
        // SHA-256 generates 256-bit (32 bytes) hash
        MessageDigest messageDigest = MessageDigest.getInstance(HASH_ALGORITHM);
        return messageDigest.digest(tag);
    }

    public static byte[] deriveSalt(byte[] tag) {
        byte[] salt = new byte[256];
        System.arraycopy(tag, 1, salt, 0, 64);
        return salt;
    }

    public void fetchMessages() {
        boolean stopName = true;

        byte[] message;

        try {
            message = bulletinBoard.getMessage(ba.getBoxNumber(), ba.getTag());
        } catch (NoSuchAlgorithmException | RemoteException e) {
            throw new RuntimeException(e);
        }

        System.out.println("fetch " + stopName);

        while (message != null) {
            String newMessage = null;

            try {
                newMessage = new String(SecurityManager.decryptMessage(message, ba.getSecretKey()));
            } catch (InvalidKeyException | NoSuchPaddingException | NoSuchAlgorithmException |
                     IllegalBlockSizeException | BadPaddingException e) {
                throw new RuntimeException(e);
            }

            byte[] tag = newMessage.substring(TAG_SUBSTRING_START, TAG_SUBSTRING_END).getBytes();
            int boxNumber = Integer.parseInt(newMessage.substring(BOX_NUMBER_SUBSTRING_START, BOX_NUMBER_SUBSTRING_END));
            String payload = newMessage.substring(BOX_NUMBER_SUBSTRING_END);

            ba.setTag(tag);
            ba.setBoxNumber(boxNumber);
            System.out.println("Receiver: " + Arrays.toString(tag) + ", " + boxNumber);
            ba.setSecretKey(SecurityManager.getSymmetricKey(Base64.getEncoder().encodeToString(ba.getSecretKey().getEncoded()), deriveSalt(tag)));

            System.out.println("found " + payload + " " + stopName);
            if (!stopName) chat.add(new ChatMessage(name, payload));
            else {
                setName(payload);
                stopName = false;
            }
        }
    }

    public void bump(String bumpstring, String passphrase) {
        int boxNumber_AB;
        int boxNumber_BA;

        try {
            boxNumber_AB = Math.abs(RandomStringGenerator.deriveIntFromPasshrase(bumpstring)) % bulletinBoard.getAmountOfMailboxes();
            boxNumber_BA = Math.abs(RandomStringGenerator.deriveIntFromPasshrase(new StringBuilder(bumpstring).reverse().toString())) % bulletinBoard.getAmountOfMailboxes();
            System.out.println("Boxnumber AB: " + boxNumber_AB + ", BoxNumber BA: " + boxNumber_BA);
        } catch(RemoteException ex) {
            throw new RuntimeException();
        }

        byte[] tag_AB = RandomStringGenerator.deriveBytesFromPassphrase(passphrase);
        byte[] tag_BA = RandomStringGenerator.deriveBytesFromPassphrase(new StringBuilder(passphrase).reverse().toString());
        System.out.println("Tag AB: " + tag_AB + ", Tag BA: " + tag_BA);

        SecretKey secretKey_AB = SecurityManager.getSymmetricKey(passphrase, tag_AB);
        SecretKey secretKey_BA = SecurityManager.getSymmetricKey(passphrase, tag_BA);

        this.ab = new ConnectionInfo(boxNumber_AB, tag_AB, secretKey_AB);
        this.ba = new ConnectionInfo(boxNumber_BA, tag_BA, secretKey_BA);
    }

    public void bumpBack(String bumpstring, String passphrase) {
        int boxNumber_AB;
        int boxNumber_BA;

        try {
            boxNumber_BA = Math.abs(RandomStringGenerator.deriveIntFromPasshrase(bumpstring)) % bulletinBoard.getAmountOfMailboxes();
            boxNumber_AB = Math.abs(RandomStringGenerator.deriveIntFromPasshrase(new StringBuilder(bumpstring).reverse().toString())) % bulletinBoard.getAmountOfMailboxes();
            System.out.println("Boxnumber AB: " + boxNumber_AB + ", BoxNumber BA: " + boxNumber_BA);
        } catch(RemoteException ex) {
            throw new RuntimeException();
        }

        byte[] tag_BA = RandomStringGenerator.deriveBytesFromPassphrase(passphrase);
        byte[] tag_AB = RandomStringGenerator.deriveBytesFromPassphrase(new StringBuilder(passphrase).reverse().toString());
        System.out.println("Tag AB: " + tag_AB + ", Tag BA: " + tag_BA);

        SecretKey secretKey_AB = SecurityManager.getSymmetricKey(passphrase, tag_AB);
        SecretKey secretKey_BA = SecurityManager.getSymmetricKey(passphrase, tag_BA);

        this.ab = new ConnectionInfo(boxNumber_AB, tag_AB, secretKey_AB);
        this.ba = new ConnectionInfo(boxNumber_BA, tag_BA, secretKey_BA);
    }

    public void startFetcher() {
        fetcher.start();
    }
}
