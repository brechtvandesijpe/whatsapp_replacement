package be.kuleuven;

import be.kuleuven.instances.Mailbox;
import be.kuleuven.interfaces.BulletinBoardInterface;

import java.rmi.RemoteException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ApplicationServer implements Runnable {
    @Override
    public void run() {

    }

    public byte[] getMessage(int boxNumber, byte[] tag) throws NoSuchAlgorithmException, RemoteException {
        MessageDigest messageDigest = MessageDigest.getInstance(HASH_ALGORITHM);
        byte[] hashedTag = messageDigest.digest(tag);
        Mailbox targetMailbox = BulletinBoardImpl.bulletinBoard[boxNumber];
        return targetMailbox.getMessageByTag(hashedTag);
    }

    public void postMessage(int boxNumber, byte[] message, byte[] hashedTag) throws RemoteException {

    }

    public void leave(String clientName) throws RemoteException {

    }

    public int getAmountOfMailboxes() throws RemoteException {

    }
}
