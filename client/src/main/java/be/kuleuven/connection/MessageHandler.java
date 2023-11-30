package be.kuleuven.connection;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class MessageHandler {
    private static final String HASH_ALGORITHM = "SHA-256";

    public static byte[] hashTag(byte[] tag) throws NoSuchAlgorithmException {
        // SHA-256 generates 256-bit (32 bytes) hash
        MessageDigest messageDigest = MessageDigest.getInstance(HASH_ALGORITHM);
        return messageDigest.digest(tag);
    }

    public static byte[] encryptMessage(byte[] messageBytes, SecretKey secretKey) throws NoSuchPaddingException,
            NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        return SecurityManager.encryptMessage(messageBytes, secretKey);
    }

    public static void deriveAndUpdateSecretKey(ConnectionInfo ab) {
        String encodedSecretKey = Base64.getEncoder().encodeToString(ab.getSecretKey().getEncoded());
        ab.setSecretKey(SecurityManager.getSymmetricKey(encodedSecretKey, deriveSalt(ab.getTag())));
        System.out.println("Nieuwe SecretKey: " + encodedSecretKey);
    }

    // Derive a salt from a tag, could be a random salt aswell but then we have to save it somewhere
    public static byte[] deriveSalt(byte[] tag) {
        byte[] salt = new byte[256];
        System.arraycopy(tag, 1, salt, 0, 64);
        return salt;
    }
}