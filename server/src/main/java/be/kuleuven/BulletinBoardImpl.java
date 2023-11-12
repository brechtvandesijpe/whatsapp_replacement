package be.kuleuven;

import be.kuleuven.Instances.*;
import be.kuleuven.Interfaces.BulletinBoardInterface;

import java.net.*;
import java.rmi.*;
import java.rmi.server.*;
import java.security.*;

public class BulletinBoardImpl extends UnicastRemoteObject implements BulletinBoardInterface {

    private static final int NUM_MAILBOXES = 64;
    // Host name for the RMI registry
    private static final String HOST_NAME = "localhost";
    // RMI service name
    private static final String SERVICE = "chat";
    private static final String HASH_ALGORITHM = "SHA-256";
    private static final int REGISTRY_PORT = 1099;

    private static Mailbox[] bulletinBoard;

    public BulletinBoardImpl() throws RemoteException {
        BulletinBoardImpl.bulletinBoard = new Mailbox[NUM_MAILBOXES];
        for (int i = 0; i < NUM_MAILBOXES; i++) {
            bulletinBoard[i] = new Mailbox();
        }
        System.out.println("BulletinBoard initialized succesfully.");
    }

    @Override
    public byte[] getMessage(int boxNumber, byte[] tag) throws NoSuchAlgorithmException, RemoteException {
        MessageDigest messageDigest = MessageDigest.getInstance(HASH_ALGORITHM);
        byte[] hashedTag = messageDigest.digest(tag);
        Mailbox targetMailbox = BulletinBoardImpl.bulletinBoard[boxNumber];
        return targetMailbox.getMessageByTag(hashedTag);
    }

    @Override
    public void postMessage(int boxNumber, byte[] message, byte[] hashedTag) throws RemoteException {
        // Store the message in the specified mailbox using the hashed tag
        bulletinBoard[boxNumber].storeMessage(hashedTag, message);
    }

    @Override
    public void leave(String clientName) throws RemoteException {
        System.err.println(clientName + " has left.");
    }

    @Override
    public int getAmountOfMailboxes() throws RemoteException {
        return bulletinBoard.length;
    }

    public static void main(String[] args) {
        initializeRMIRegistry();
        startBulletinBoardServer();
    }

    public static void initializeRMIRegistry() {
        try {
            // Create an RMI registry on the specified port
            java.rmi.registry.LocateRegistry.createRegistry(REGISTRY_PORT);
            System.out.println("RMI registry has been initialized.");
        } catch (RemoteException e) {
            e.printStackTrace();
            System.err.println("Error occured when initializing BulletinBoardServer");
        }
    }

    private static void startBulletinBoardServer() {
        try {
            BulletinBoardImpl bulletinBoardImpl = new BulletinBoardImpl();
            String registry = "rmi://" + BulletinBoardImpl.HOST_NAME + "/" + SERVICE + "/";
            Naming.rebind(registry, bulletinBoardImpl);
            System.out.println("Server is running and bound to: " + registry);
        } catch (RemoteException | MalformedURLException e) {
            System.err.println("Error occured when starting BulletinBoardServer");
            e.printStackTrace();
        }
    }

    public static Mailbox[] getBulletinBoard() {
        return bulletinBoard;
    }

    public static void setBulletinBoard(Mailbox[] bulletinBoard) {
        BulletinBoardImpl.bulletinBoard = bulletinBoard;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Bulletin Board Information:\n");
        sb.append("Host Name: ").append(HOST_NAME).append("\n");
        sb.append("Service Name: ").append(SERVICE).append("\n");
        sb.append("Number of Mailboxes: ").append(NUM_MAILBOXES).append("\n");
        sb.append("Bulletin Board Status:\n");

        for (int i = 0; i < NUM_MAILBOXES; i++) {
            sb.append("Mailbox ").append(i).append(": ");
            sb.append(bulletinBoard[i].toString()).append("\n");
        }

        return sb.toString();
    }
}
