package be.kuleuven.model;

import be.kuleuven.connection.SecurityManager;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class ChatMessage {
    private final String username;
    private final String message;

    public ChatMessage(String username, String message) {
        this.username = username;
        this.message = message;
    }

    public String getUsername() {
        return username;
    }

    public String getMessage() {
        return message;
    }

    public byte[] encrypt(SecretKey secretKey) {
        try {
            return SecurityManager.encryptMessage(message.getBytes(), secretKey);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | BadPaddingException |
                 IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        }
    }

    private static char getRandomString() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_+=<>?";

        // Kies willekeurig een teken uit de lijst
        int rnd = (int) (Math.random() * characters.length());

        return characters.charAt(rnd);
    }

    @Override
    public String toString() {
        return username + ": " + message;
    }
}
