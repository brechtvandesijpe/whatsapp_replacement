package be.kuleuven.connection;

import be.kuleuven.interfaces.BulletinBoardInterface;
import be.kuleuven.model.ChatMessage;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import org.json.JSONObject;

public abstract class Connection {
    protected static final int TAG_SUBSTRING_START = 0;
    protected static final int TAG_SUBSTRING_END = 256;
    protected static final int BOX_NUMBER_SUBSTRING_START = 256;
    protected static final int BOX_NUMBER_SUBSTRING_END = 258;
    protected static final int MESSAGE_FETCH_INTERVAL = 200;
    protected ConnectionInfo ab;
    protected ConnectionInfo ba;
    protected final BulletinBoardInterface bulletinBoard;

    public Connection(BulletinBoardInterface bulletinBoard) {
        this.bulletinBoard = bulletinBoard;
    }

    protected static char getRandomChar() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_+=<>?";

        // Kies willekeurig een teken uit de lijst
        int rnd = (int) (Math.random() * characters.length());

        return characters.charAt(rnd);
    }

    protected static String generateRandomTag() {
        StringBuilder tagBuilder = new StringBuilder();
        for (int i = 0; i < 256; i++) {
            tagBuilder.append(getRandomChar());
        }
        return tagBuilder.toString();
    }

    public String transformMessage(String message) throws RemoteException {
        // Generate a random tag
        String randomTag = generateRandomTag();

        int newBoxNumber = 0;
        newBoxNumber = (int) (Math.random() * bulletinBoard.getAmountOfMailboxes());

        // Update the BulletinEntry for the given friend
        ab.setTag(randomTag.getBytes());
        ab.setBoxNumber(newBoxNumber);

        // Build the final message format
        return randomTag + String.format("%02d", newBoxNumber) + message;
    }

    public void sendMessage(ChatMessage chatMessage) throws RemoteException {
        int boxNumber = ab.getBoxNumber();
        byte[] tag = Arrays.copyOf(ab.getTag(), ab.getTag().length);
        String transformedMessage = transformMessage(chatMessage.getMessage());
        byte[] hashedMessage;

        try {
            hashedMessage = MessageHandler.encryptMessage(transformedMessage.getBytes(), ab.getSecretKey());
        } catch (NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException |
                 NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        byte[] hashedTag;
        try {
            hashedTag = MessageHandler.hashTag(tag);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        MessageHandler.deriveAndUpdateSecretKey(ab);
        System.out.println("boxNumber = " + boxNumber + ", hashedTag = " + Arrays.toString(hashedTag));
        bulletinBoard.postMessage(boxNumber, hashedMessage, hashedTag);
    }

    public abstract void fetchMessages();

    protected void toJSONObject(JSONObject output) {
        output.put("ab", ab.toJSONObject());
        output.put("ba", ba.toJSONObject());
    }
}
