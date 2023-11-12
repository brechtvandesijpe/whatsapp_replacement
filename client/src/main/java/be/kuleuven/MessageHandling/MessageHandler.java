package be.kuleuven.MessageHandling;

import be.kuleuven.*;
import be.kuleuven.Instances.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import be.kuleuven.Managers.SecurityManager;

public class MessageHandler {
    private Client client;
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

    public void sendMessage(String contactName, String message) throws NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, RemoteException {
        if(shouldSendMessage(message)){
            BulletinEntry bulletinEntry_AB = client.getBulletEntry_AB_from(contactName);

            int boxNumber_AB = bulletinEntry_AB.getBoxNumber();
            byte[] tag_AB = Arrays.copyOf(bulletinEntry_AB.getTag(), bulletinEntry_AB.getTag().length);
            String transformedMessage = client.transformMessage(contactName, message);

            byte[] hashedMessage = encryptMessage(transformedMessage.getBytes(), bulletinEntry_AB.getSecretKey());
            byte[] hashedTag = hashTag(tag_AB);

            // TODO : afleiden symm & upload nr board
        }
    }

    private byte[] hashTag(byte[] tag) throws NoSuchAlgorithmException {
        // SHA-256 generates 256-bit (32 bytes) hash
        MessageDigest messageDigest = MessageDigest.getInstance(HASH_ALGORITHM);
        return messageDigest.digest(tag);
    }

    private byte[] encryptMessage(byte[] messageBytes, SecretKey secretKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        return SecurityManager.encryptMessage(messageBytes, secretKey);
    }

    private boolean shouldSendMessage(String message) {
        return !message.isEmpty();
    }

    @Override
    public String toString() {
        return "MessageHandler{" +
                "client=" + client +
                '}';
    }
}
