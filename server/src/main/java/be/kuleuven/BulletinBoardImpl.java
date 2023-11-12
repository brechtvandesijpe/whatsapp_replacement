package be.kuleuven;

import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.security.*;

public class BulletinBoardImpl extends UnicastRemoteObject implements BulletinBoardInterface{

    public BulletinBoardImpl() throws RemoteException {

    }

    @Override
    public byte[] getMessage(int boxNumber, byte[] tag) throws NoSuchAlgorithmException, RemoteException {
        return new byte[0];
    }

    @Override
    public void postMessage(int boxNumber, byte[] message, byte[] hashedTag) throws RemoteException {

    }

    @Override
    public void leave(String clientName) throws RemoteException {

    }

    @Override
    public int getAmountOfMailboxes() throws RemoteException {
        return 0;
    }
}
