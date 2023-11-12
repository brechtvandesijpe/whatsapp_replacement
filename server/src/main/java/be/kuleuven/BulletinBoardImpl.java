package be.kuleuven;

import be.kuleuven.Instances.*;

import java.rmi.*;
import java.rmi.server.*;
import java.security.*;

public class BulletinBoardImpl extends UnicastRemoteObject implements BulletinBoardInterface{

    private static final int NUM_MAILBOXES = 64;
    // Host name for the RMI registry
    private static final String HOST_NAME = "localhost";
    // RMI service name
    private static final String SERVICE = "chat";

    private static Mailbox[] bulletinBoard;

    public BulletinBoardImpl() throws RemoteException {
        BulletinBoardImpl.bulletinBoard = new Mailbox[NUM_MAILBOXES];
    }

    @Override
    public byte[] getMessage(int boxNumber, byte[] tag) throws NoSuchAlgorithmException, RemoteException {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
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
