package be.kuleuven.Interfaces;

import java.rmi.*;
import java.security.*;

public interface BulletinBoardInterface {
    byte[] getMessage(int boxNumber, byte[] tag) throws NoSuchAlgorithmException, RemoteException;
    void postMessage(int boxNumber, byte[] message, byte[] hashedTag) throws RemoteException;
    void leave(String clientName) throws RemoteException;
    int getAmountOfMailboxes() throws RemoteException;
}
