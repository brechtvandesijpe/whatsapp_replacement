package be.kuleuven.Util;

import java.nio.charset.StandardCharsets;
import java.security.*;

// Utility class for generating random strings for bumpstring, and deriving values from strings
public class RandomStringGenerator {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    // Generate a random bump string of the specified length using characters
    public static String generateRandomString(int length) {
        StringBuilder stringBuilder = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int randomIndex = RANDOM.nextInt(CHARACTERS.length());
            char randomChar = CHARACTERS.charAt(randomIndex);
            stringBuilder.append(randomChar);
        }

        return stringBuilder.toString();
    }

    // Derive an integer from a passphrase using SHA-256 (used to determine the initial common attributes: box, tag, key)
    public static int deriveIntFromPasshrase(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            // Converteer byte-array to an integer
            int result = 0;
            for (byte b : encodedhash) {
                result = result * 256 + (b & 0xFF);
            }

            return result;
        } catch (NoSuchAlgorithmException e) {
            System.err.println("deriveIntFromPasshrase failed");
            e.printStackTrace();
            return 0;
        }
    }

    // Derive bytes from a passphrase using SHA-256
    public static byte[] deriveBytesFromPassphrase(String passphrase) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] passphraseBytes = passphrase.getBytes(StandardCharsets.UTF_8);
            byte[] hash = digest.digest(passphraseBytes);

            return hash;
        } catch (NoSuchAlgorithmException e) {
            System.err.println("deriveBytesFromPassphrase failed");
            e.printStackTrace();
            return null;
        }
    }
}