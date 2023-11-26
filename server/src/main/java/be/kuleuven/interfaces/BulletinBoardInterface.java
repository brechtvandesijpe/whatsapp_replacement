package be.kuleuven.interfaces;

import java.rmi.*;
import java.security.*;

public interface BulletinBoardInterface extends Remote {
    // Retrieve a message from the specified mailbox using the tag
    byte[] getMessage(int boxNumber, byte[] tag) throws NoSuchAlgorithmException, RemoteException;
    // Post a message to the specified mailbox using the hashed tag
    void postMessage(int boxNumber, byte[] message, byte[] hashedTag) throws RemoteException;
    // Notify the server when a client leaves the chat
    void leave(String clientName) throws RemoteException;
    // Get the total number of mailboxes on the bulletin board
    int getAmountOfMailboxes() throws RemoteException;
}
