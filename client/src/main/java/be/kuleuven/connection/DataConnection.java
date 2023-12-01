package be.kuleuven.connection;

import be.kuleuven.interfaces.BulletinBoardInterface;
import be.kuleuven.model.Chat;
import be.kuleuven.model.ChatMessage;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

public class DataConnection extends Connection {
    private Chat chat;
    private final String name;
    protected final PeriodicMessageFetcher fetcher;
    public DataConnection(ConnectionInfo ab, ConnectionInfo ba, BulletinBoardInterface bulletinBoard, Chat chat, String name) {
        super(bulletinBoard);
        super.ab = ab;
        super.ba = ba;
        this.chat = chat;
        this.name = name;
        this.fetcher = new PeriodicMessageFetcher(this);
        startFetcher();
    }

    @Override
    public void sendMessage(ChatMessage chatMessage) {
        chat.add(chatMessage);
        super.sendMessage(chatMessage);
    }

    @Override
    public void fetchMessages() {
        byte[] message = null;

        try {
            message = bulletinBoard.getMessage(ba.getBoxNumber(), ba.getTag());
        } catch (NoSuchAlgorithmException | RemoteException e) {
//            System.out.println(e.getMessage());
        }

        while (message != null) {
            String newMessage = null;

            try {
                newMessage = new String(SecurityManager.decryptMessage(message, ba.getSecretKey()));

                byte[] tag = newMessage.substring(TAG_SUBSTRING_START, TAG_SUBSTRING_END).getBytes();
                int boxNumber = Integer.parseInt(newMessage.substring(BOX_NUMBER_SUBSTRING_START, BOX_NUMBER_SUBSTRING_END));
                String payload = newMessage.substring(BOX_NUMBER_SUBSTRING_END);

                ba.setTag(tag);
                ba.setBoxNumber(boxNumber);
                ba.setSecretKey(SecurityManager.getSymmetricKey(Base64.getEncoder().encodeToString(ba.getSecretKey().getEncoded()), deriveSalt(tag)));

                chat.add(new ChatMessage(name, payload));
            } catch (InvalidKeyException | NoSuchPaddingException | NoSuchAlgorithmException |
                     IllegalBlockSizeException | BadPaddingException e) {
//                System.out.println(e.getMessage());
            }

            try {
                message  = bulletinBoard.getMessage(ba.getBoxNumber(), ba.getTag());
            } catch (NoSuchAlgorithmException | RemoteException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static byte[] deriveSalt(byte[] tag) {
        byte[] salt = new byte[256];
        System.arraycopy(tag, 1, salt, 0, 64);
        return salt;
    }

    public void startFetcher() {
        fetcher.start();
    }
}
