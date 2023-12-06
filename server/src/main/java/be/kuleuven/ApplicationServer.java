package be.kuleuven;

import be.kuleuven.instances.Mailbox;
import be.kuleuven.interfaces.BulletinBoardInterface;

import java.rmi.RemoteException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ApplicationServer extends Thread {
    private static final int NUM_MAILBOXES = 64;
    private static Mailbox[] bulletinBoard;

    public ApplicationServer() {
        bulletinBoard = new Mailbox[NUM_MAILBOXES];
        for (int i = 0; i < NUM_MAILBOXES; i++) {
            bulletinBoard[i] = new Mailbox();
        }
        System.out.println("BulletinBoard initialized succesfully.");
    }

    public byte[] getMessage(int boxNumber, byte[] tag) throws NoSuchAlgorithmException, RemoteException {
        Mailbox targetMailbox = bulletinBoard[boxNumber];
        return targetMailbox.getMessageByTag(tag);
    }

    public void postMessage(int boxNumber, byte[] message, byte[] hashedTag) throws RemoteException {
        bulletinBoard[boxNumber].storeMessage(hashedTag, message);
    }

    public int getAmountOfMailboxes() throws RemoteException {
        return NUM_MAILBOXES;
    }

    public boolean isFull() {
        for (Mailbox mailbox : bulletinBoard) {
            if (!mailbox.isFUll()) {
                return false;
            }
        }
        return true;
    }

    public boolean isEmpty() {
        for (Mailbox mailbox : bulletinBoard) {
            if (!mailbox.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Bulletin Board Information:\n");
        sb.append("Number of Mailboxes: ").append(NUM_MAILBOXES).append("\n");
        sb.append("Bulletin Board Status:\n");

        for (int i = 0; i < NUM_MAILBOXES; i++) {
            sb.append("Mailbox ").append(i).append(": ");
            sb.append(bulletinBoard[i].toString()).append("\n");
        }

        return sb.toString();
    }
}
