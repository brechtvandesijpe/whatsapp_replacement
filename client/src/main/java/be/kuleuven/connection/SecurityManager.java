package be.kuleuven.connection;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.*;
import java.security.spec.*;

public class SecurityManager {

    private final static String HASH_ALGORITHM = "SHA-256";
    private final static String ENCRYPTION_ALGORITHM = "AES";
    private final static String PBKDF_ALGORITHM = "PBKDF2WithHmacSHA1";

    // Hash a string
    public static byte[] hash(String unhashed) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance(HASH_ALGORITHM);
        return messageDigest.digest(unhashed.getBytes());
    }

    // Password-Based Key Derivation Function 2 (PBKDF2).
    // Derive a symmetric key using PBKDF2
    public static SecretKey getSymmetricKey(String code, byte[] salt) {
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(PBKDF_ALGORITHM);
            KeySpec spec = new PBEKeySpec(code.toCharArray(), salt, 2048, 256);
            SecretKey tmp = factory.generateSecret(spec);
            return new SecretKeySpec(tmp.getEncoded(), ENCRYPTION_ALGORITHM);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Decrypt an encrypted message
    public static byte[] decryptMessage(byte[] encryptedMessageBytes, SecretKey symmetricKey)
            throws InvalidKeyException,
            NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, symmetricKey);
        return cipher.doFinal(encryptedMessageBytes);
    }

    // Encrypt a plain-text message
    public static byte[] encryptMessage(byte[] plaintTextBytes, SecretKey symmetricKey)
            throws InvalidKeyException,
            NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, symmetricKey);
        return cipher.doFinal(plaintTextBytes);
    }
}