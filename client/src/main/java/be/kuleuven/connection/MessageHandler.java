package be.kuleuven.connection;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class MessageHandler {
    private static final int TAG_SUBSTRING_START = 0;
    private static final int TAG_SUBSTRING_END = 256;
    private static final int BOX_NUMBER_SUBSTRING_START = 256;
    private static final int BOX_NUMBER_SUBSTRING_END = 258;
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

    public static void deriveAndUpdateSecretKey(BulletinEntry bulletinEntry_AB) {
        String encodedSecretKey = Base64.getEncoder().encodeToString(bulletinEntry_AB.getSecretKey().getEncoded());
        bulletinEntry_AB.setSecretKey(SecurityManager.getSymmetricKey(encodedSecretKey, deriveSalt(bulletinEntry_AB.getTag())));
        System.out.println("Nieuwe SecretKey: " + encodedSecretKey);
    }

    // Derive a salt from a tag, could be a random salt aswell but then we have to save it somewhere
    public static byte[] deriveSalt(byte[] tag) {
        byte[] salt = new byte[256];
        System.arraycopy(tag, 1, salt, 0, 64);
        return salt;
    }
}
