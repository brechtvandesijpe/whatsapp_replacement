package be.kuleuven.connection;

import be.kuleuven.UserInterface;
import be.kuleuven.interfaces.BulletinBoardInterface;
import be.kuleuven.model.Chat;
import be.kuleuven.model.ChatMessage;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

public class MetaConnection extends Connection {
    private Chat chat;
    private final UserInterface ui;
    private final Client client;
    private final ConnectionHandler connectionHandler;
    protected final PeriodicMessageFetcher fetcher;

    public MetaConnection(BulletinBoardInterface bulletinBoard, Chat chat, UserInterface ui, Client client,
                            ConnectionHandler connectionHandler) {
        super(bulletinBoard);
        this.chat = chat;
        this.ui = ui;
        this.client = client;
        this.connectionHandler = connectionHandler;
        this.fetcher = new PeriodicMessageFetcher(this);
    }

    private ConnectionInfo[] calculateBumpConnectionInfo(String bumpstring, String passphrase) {
        int boxNumber_AB;
        int boxNumber_BA;

        try {
            boxNumber_AB = Math.abs(RandomStringGenerator.deriveIntFromPasshrase(bumpstring)) % bulletinBoard.getAmountOfMailboxes();
            boxNumber_BA = Math.abs(RandomStringGenerator.deriveIntFromPasshrase(new StringBuilder(bumpstring).reverse().toString())) % bulletinBoard.getAmountOfMailboxes();
        } catch(RemoteException ex) {
            throw new RuntimeException();
        }

        byte[] tag_AB = RandomStringGenerator.deriveBytesFromPassphrase(passphrase);
        byte[] tag_BA = RandomStringGenerator.deriveBytesFromPassphrase(new StringBuilder(passphrase).reverse().toString());

        SecretKey secretKey_AB = SecurityManager.getSymmetricKey(passphrase, tag_AB);
        SecretKey secretKey_BA = SecurityManager.getSymmetricKey(passphrase, tag_BA);

        ConnectionInfo[] output = new ConnectionInfo[2];
        output[0] = new ConnectionInfo(boxNumber_AB, tag_AB, secretKey_AB);
        output[1] = new ConnectionInfo(boxNumber_BA, tag_BA, secretKey_BA);

        return output;
    }

    public void bump(String bumpstring, String passphrase) {
        ConnectionInfo[] metaConnectionInfo = calculateBumpConnectionInfo(bumpstring, passphrase);
        this.ab = metaConnectionInfo[0];
        this.ba = metaConnectionInfo[1];

        String dataBumpstring = RandomStringGenerator.generateRandomString(10);
        ConnectionInfo[] dataConnectionInfo = calculateBumpConnectionInfo(dataBumpstring, dataBumpstring);

        connectionHandler.setConnectionInfo(dataConnectionInfo[0], dataConnectionInfo[1]);
        String name = connectionHandler.getName();
        sendMessage(new ChatMessage(name, "name," + bumpstring + "," + client.getUsername()));
        sendMessage(new ChatMessage(name, "databumpstring," + dataBumpstring));
        startFetcher();
    }

    private ConnectionInfo[] calculateBumpBackConnectionInfo(String bumpstring, String passphrase) {
        int boxNumber_AB;
        int boxNumber_BA;

        try {
            boxNumber_BA = Math.abs(RandomStringGenerator.deriveIntFromPasshrase(bumpstring)) % bulletinBoard.getAmountOfMailboxes();
            boxNumber_AB = Math.abs(RandomStringGenerator.deriveIntFromPasshrase(new StringBuilder(bumpstring).reverse().toString())) % bulletinBoard.getAmountOfMailboxes();
        } catch(RemoteException ex) {
            throw new RuntimeException();
        }

        byte[] tag_BA = RandomStringGenerator.deriveBytesFromPassphrase(passphrase);
        byte[] tag_AB = RandomStringGenerator.deriveBytesFromPassphrase(new StringBuilder(passphrase).reverse().toString());

        SecretKey secretKey_AB = SecurityManager.getSymmetricKey(passphrase, tag_AB);
        SecretKey secretKey_BA = SecurityManager.getSymmetricKey(passphrase, tag_BA);

        ConnectionInfo[] output = new ConnectionInfo[2];
        output[0] = new ConnectionInfo(boxNumber_AB, tag_AB, secretKey_AB);
        output[1] = new ConnectionInfo(boxNumber_BA, tag_BA, secretKey_BA);

        return output;
    }

    public void bumpBack(String bumpstring, String passphrase) {
        ConnectionInfo[] metaConnectionInfo = calculateBumpBackConnectionInfo(bumpstring, passphrase);
        this.ab = metaConnectionInfo[0];
        this.ba = metaConnectionInfo[1];

        String name = connectionHandler.getName();
        sendMessage(new ChatMessage(name, "name," + bumpstring + "," + client.getUsername()));
        startFetcher();
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
                ba.setSecretKey(SecurityManager.getSymmetricKey(Base64.getEncoder().encodeToString(ba.getSecretKey().getEncoded()), MessageHandler.deriveSalt(tag)));

                String[] parts = payload.split(",");
                System.out.println("Metafetcher got Parts: " + Arrays.toString(parts));
                if (parts[0].equals("name")) setName(parts[1], parts[2]);
                if (parts[0].equals("databumpstring")) setDataBumpstring(parts[1]);
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

    private void setDataBumpstring(String bumpstring) {
        ConnectionInfo[] dataConnectionInfo = calculateBumpBackConnectionInfo(bumpstring, bumpstring);
        connectionHandler.setConnectionInfo(dataConnectionInfo[0], dataConnectionInfo[1]);
    }

    private void setName(String oldName, String name) {
        int index = ui.addContact(name);
        client.changeChatName(index, oldName);
        connectionHandler.setName(name);
    }

    public void startFetcher() {
        fetcher.start();
    }
}
