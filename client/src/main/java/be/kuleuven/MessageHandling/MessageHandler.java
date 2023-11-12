package be.kuleuven.MessageHandling;

import be.kuleuven.*;
import be.kuleuven.Instances.*;

import javax.crypto.*;
import java.rmi.*;
import java.security.*;
import java.util.*;
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

            deriveAndUpdateSecretKey(bulletinEntry_AB);
            postMessageToBulletinBoard(boxNumber_AB, hashedMessage, hashedTag, message, contactName);
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

    private void deriveAndUpdateSecretKey(BulletinEntry bulletinEntry_AB) {
        String encodedSecretKey = Base64.getEncoder().encodeToString(bulletinEntry_AB.getSecretKey().getEncoded());
        bulletinEntry_AB.setSecretKey(SecurityManager.getSymmetricKey(encodedSecretKey, deriveSalt(bulletinEntry_AB.getTag())));
    }

    private byte[] deriveSalt(byte[] tag) {
        byte[] salt = new byte[256];
        System.arraycopy(tag, 1, salt, 0, 64);
        return salt;
    }

    private void postMessageToBulletinBoard(int boxNumber_AB, byte[] hashedMessage, byte[] hashedTag, String message, String contactName) {
        // TODO
    }

    @Override
    public String toString() {
        return "MessageHandler{" +
                "client=" + client +
                '}';
    }
}
