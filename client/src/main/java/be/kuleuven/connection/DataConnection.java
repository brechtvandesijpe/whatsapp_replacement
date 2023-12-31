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
import java.util.Base64;
import org.json.JSONObject;

public class DataConnection extends Connection {
    private Chat chat;
    private String name;
    private Client client;

    public DataConnection(ConnectionInfo ab, ConnectionInfo ba, BulletinBoardInterface bulletinBoard, Chat chat, String name, Client client) {
        super(bulletinBoard);
        super.ab = ab;
        super.ba = ba;
        this.chat = chat;
        this.name = name;
        this.client = client;
    }

    public JSONObject toJSONObject() {
        JSONObject output = new JSONObject();
        output.put("name", name);
        super.toJSONObject(output);
        return output;
    }

    public DataConnection(JSONObject data, BulletinBoardInterface bulletinBoard, Chat chat, Client client) {
        super(bulletinBoard);
        ConnectionInfo ab = new ConnectionInfo(data.getJSONObject("ab"));
        ConnectionInfo ba = new ConnectionInfo(data.getJSONObject("ba"));
        super.ab = ab;
        super.ba = ba;
        this.chat = chat;
        this.client = client;
        name = data.getString("name");
    }

    @Override
    public void sendMessage(ChatMessage chatMessage) throws RemoteException {
        super.sendMessage(chatMessage);
        chat.add(chatMessage);
    }

    @Override
    public void fetchMessages() {
        byte[] message = null;

        try {
            message = bulletinBoard.getMessage(ba.getBoxNumber(), ba.getTag());

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
                    client.saveState();
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
        } catch (NoSuchAlgorithmException e) {
            // IGNORE
        } catch (RemoteException e) {
            client.setRecoveryMode();
        }
    }

    public static byte[] deriveSalt(byte[] tag) {
        byte[] salt = new byte[256];
        System.arraycopy(tag, 1, salt, 0, 64);
        return salt;
    }

    public void startFetcher() {
        Runnable task = this::fetchMessages;

        fetcher = new Thread(() -> {
            while (true) {
                try {
                    task.run();
                    Client.getInstance().saveState();
                    Thread.sleep(MESSAGE_FETCH_INTERVAL);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    // Handle interruption if needed
                    break;
                }
            }
        });

        fetcher.start();
    }
}
