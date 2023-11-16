package be.kuleuven.Util;

import java.nio.charset.StandardCharsets;
import java.security.*;

public class RandomStringGenerator {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generateRandomString(int length) {
        StringBuilder stringBuilder = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int randomIndex = RANDOM.nextInt(CHARACTERS.length());
            char randomChar = CHARACTERS.charAt(randomIndex);
            stringBuilder.append(randomChar);
        }

        return stringBuilder.toString();
    }

    public static int deriveIntFromPasshrase(String input) {
        try {
            // Maak een SHA-256 MessageDigest object
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // Krijg de byte-array van de inputstring
            byte[] encodedhash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            // Converteer de byte-array naar een integer
            int result = 0;
            for (byte b : encodedhash) {
                result = result * 256 + (b & 0xFF);
            }

            return result;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static byte[] deriveBytesFromPassphrase(String passphrase) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // Converteer de passphrase naar bytes
            byte[] passphraseBytes = passphrase.getBytes(StandardCharsets.UTF_8);

            // Bereken de hash van de passphrase
            byte[] hash = digest.digest(passphraseBytes);

            // Retourneer de resulterende hash als afgeleide bytes
            return hash;
        } catch (NoSuchAlgorithmException e) {
            // Handle eventuele uitzonderingen bij het selecteren van de hashfunctie
            e.printStackTrace();
            return null; // Of gooi een aangepaste uitzondering op
        }
    }
}