package be.kuleuven;

import be.kuleuven.instances.*;
import be.kuleuven.interfaces.BulletinBoardInterface;

import java.net.*;
import java.nio.ByteBuffer;
import java.rmi.*;
import java.rmi.server.*;
import java.security.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class BulletinBoardImpl extends UnicastRemoteObject implements BulletinBoardInterface {

    // Host name for the RMI registry
    private static final String HOST_NAME = "localhost";
    // RMI service name
    private static final String SERVICE = "chat";
    private static final int REGISTRY_PORT = 1099;
    private static final String HASH_ALGORITHM = "SHA-256";
    private ApplicationServer freeServer;
    private ArrayList<ApplicationServer> servers;
    private HashMap<Integer, ApplicationServer> serverMap;
    private double occupancy;
    private int numAssigned;
    private int numTotal;


    public BulletinBoardImpl() throws RemoteException {
        super();
        freeServer = new ApplicationServer();
        servers = new ArrayList<>();
        servers.add(freeServer);
        serverMap = new HashMap<>();
        occupancy = 0.0;
        numAssigned = 0;
        numTotal = freeServer.getAmountOfMailboxes();
    }

    // Retrieve a message from a specified mailbox using the hashed tag
    @Override
    public byte[] getMessage(int boxNumber, byte[] tag) throws NoSuchAlgorithmException, RemoteException {
        numAssigned--;
        calculateOccupancy();

        MessageDigest messageDigest = MessageDigest.getInstance(HASH_ALGORITHM);
        byte[] hashedTag = messageDigest.digest(tag);

        Integer key = transform(boxNumber, hashedTag);
        ApplicationServer server = serverMap.get(key);
        while (server == null) {
            server = serverMap.get(key);
        }

        byte[] message = server.getMessage(boxNumber, hashedTag);

        if (servers.size() > 1 && server.isEmpty() && occupancy < 0.8) {
            numAssigned -= server.getAmountOfMailboxes();
            calculateOccupancy();
            server.interrupt();
            servers.remove(server);
            serverMap.remove(tag);
        }

        return message;
    }

    private int transform(int boxNumber, byte[] tag) {
        return boxNumber + Arrays.hashCode(tag);
    }

    // Post a message to a specified mailbox using the hashed tag
    @Override
    public void postMessage(int boxNumber, byte[] message, byte[] hashedTag) throws RemoteException {
        numAssigned++;
        calculateOccupancy();

        if (occupancy >= 0.8) {
            ApplicationServer newServer = new ApplicationServer();
            newServer.start();
            servers.add(newServer);
            numTotal += newServer.getAmountOfMailboxes();
            calculateOccupancy();
            freeServer = newServer;
        }

        freeServer.postMessage(boxNumber, message, hashedTag);
        serverMap.put(transform(boxNumber, hashedTag), freeServer);
    }

    private void calculateOccupancy() {
        occupancy = (double) numAssigned / (double) numTotal;
    }

    // Get the total number of mailboxes on the bulletin board
    @Override
    public int getAmountOfMailboxes() throws RemoteException {
        synchronized(this) {
            return freeServer.getAmountOfMailboxes();
        }
    }

    public int getAmountOfServers() {
        return servers.size();
    }

    // Main method to start the Bulletin Board server
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

    // Start the Bulletin Board server and bind it to the RMI registry
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
}
