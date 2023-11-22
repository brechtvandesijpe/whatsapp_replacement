package be.kuleuven.MessageHandling;

import be.kuleuven.*;
import be.kuleuven.Instances.*;

import javax.crypto.*;
import java.rmi.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.*;
import be.kuleuven.Managers.SecurityManager;

public class MessageHandler {
    private Client client;
    private static final int TAG_SUBSTRING_START = 0;
    private static final int TAG_SUBSTRING_END = 256;
    private static final int BOX_NUMBER_SUBSTRING_START = 256;
    private static final int BOX_NUMBER_SUBSTRING_END = 258;
    private static final String HASH_ALGORITHM = "SHA-256";

    public MessageHandler(Client client) {
        this.client = client;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public void sendMessage(String contactName, String message) throws NoSuchAlgorithmException, NoSuchPaddingException,
            IllegalBlockSizeException, BadPaddingException, InvalidKeyException, RemoteException {
        if (shouldSendMessage(message)) {
            BulletinEntry bulletinEntry_AB = client.getBulletEntry_AB_from(contactName);

            int boxNumber_AB = bulletinEntry_AB.getBoxNumber();
            byte[] tag_AB = Arrays.copyOf(bulletinEntry_AB.getTag(), bulletinEntry_AB.getTag().length);
            String transformedMessage = client.transformMessage(contactName, message);
            System.out.println("TransformedMessage: " + transformedMessage);
            System.out.println("Sender: " + Arrays.toString(tag_AB) + ", " + boxNumber_AB);
            byte[] hashedMessage = encryptMessage(transformedMessage.getBytes(), bulletinEntry_AB.getSecretKey());
            byte[] hashedTag = hashTag(tag_AB);

            deriveAndUpdateSecretKey(bulletinEntry_AB);
            postMessageToBulletinBoard(boxNumber_AB, hashedMessage, hashedTag, message, contactName);
        }
    }

    private byte[] hashTag(byte[] tag) throws NoSuchAlgorithmException {
        // SHA-256 generates 256-bit (32 bytes) hash
        MessageDigest messageDigest = MessageDigest.getInstance(HASH_ALGORITHM);
        return messageDigest.digest(tag);
    }

    private byte[] encryptMessage(byte[] messageBytes, SecretKey secretKey) throws NoSuchPaddingException,
            NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        return SecurityManager.encryptMessage(messageBytes, secretKey);
    }

    private boolean shouldSendMessage(String message) {
        return !message.isEmpty();
    }

    private void deriveAndUpdateSecretKey(BulletinEntry bulletinEntry_AB) {
        String encodedSecretKey = Base64.getEncoder().encodeToString(bulletinEntry_AB.getSecretKey().getEncoded());
        bulletinEntry_AB.setSecretKey(SecurityManager.getSymmetricKey(encodedSecretKey, deriveSalt(bulletinEntry_AB.getTag())));
        System.out.println("Nieuwe SecretKey: " + encodedSecretKey);
    }

    // Derive a salt from a tag, could be a random salt aswell but then we have to save it somewhere
    private byte[] deriveSalt(byte[] tag) {
        byte[] salt = new byte[256];
        System.arraycopy(tag, 1, salt, 0, 64);
        return salt;
    }

    private void postMessageToBulletinBoard(int boxNumber_AB, byte[] hashedMessage, byte[] hashedTag, String message, String contactName) {
        try {
            client.getBulletinBoard().postMessage(boxNumber_AB, hashedMessage, hashedTag);

            String formattedMessage = String.format("[%s]: %s\n", client.getName(), message);
            client.getUserInterface().getChatArea().append(formattedMessage);
            client.getUserInterface().getChatArea().setCaretPosition(client.getUserInterface().getChatArea().getDocument().getLength());

            List<String> messageHistory = client.getHistoryManager().getMessageHistory().get(contactName);
            messageHistory.add(formattedMessage);
        } catch (RemoteException e) {
            System.err.println("Error posting message to bulletin board: " + e.getMessage());
        }
    }

    public void getMessagesFrom(String contactName) throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, RemoteException {
        BulletinEntry bulletinEntry_BA = client.getBulletinEntry_BA_from(contactName);
        byte[] currentMessage  = client.getBulletinBoard().getMessage(bulletinEntry_BA.getBoxNumber(), bulletinEntry_BA.getTag());
        while (currentMessage != null) {
            processReceivedMessage(contactName, bulletinEntry_BA, currentMessage);
            currentMessage  = client.getBulletinBoard().getMessage(bulletinEntry_BA.getBoxNumber(), bulletinEntry_BA.getTag());
        }
    }

    private void processReceivedMessage(String name, BulletinEntry bulletinEntry_BA, byte[] thisMessage) throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException {
        System.out.println("Proberen decrypteren met key: " + bulletinEntry_BA.getSecretKey());
        String newMessage = new String(SecurityManager.decryptMessage(thisMessage, bulletinEntry_BA.getSecretKey()));

        // Extraheren van de tag van index 0 tot 32 (32 bytes)
        byte[] tag_BA = newMessage.substring(TAG_SUBSTRING_START, TAG_SUBSTRING_END).getBytes();
        int boxNumber_BA = Integer.parseInt(newMessage.substring(BOX_NUMBER_SUBSTRING_START, BOX_NUMBER_SUBSTRING_END));
        String message = newMessage.substring(BOX_NUMBER_SUBSTRING_END);

        bulletinEntry_BA.setTag(tag_BA);
        bulletinEntry_BA.setBoxNumber(boxNumber_BA);
        System.out.println("Receiver: " + Arrays.toString(tag_BA) + ", " + boxNumber_BA);
        bulletinEntry_BA.setSecretKey(SecurityManager.getSymmetricKey(Base64.getEncoder().encodeToString(bulletinEntry_BA.getSecretKey().getEncoded()), deriveSalt(tag_BA)));

        client.getUserInterface().getChatArea().append("[" + name + "]: " + message + " \n");
        client.getUserInterface().getChatArea().setCaretPosition(client.getUserInterface().getChatArea().getDocument().getLength());
        client.getHistoryManager().getMessageHistory().get(name).add("[" + name + "]: " + message + " \n");
        System.out.println("{name: " + name + ", boxNumber: " + bulletinEntry_BA.getBoxNumber() + "}");
    }

    @Override
    public String toString() {
        return "MessageHandler{" +
                "client=" + client +
                '}';
    }
}
