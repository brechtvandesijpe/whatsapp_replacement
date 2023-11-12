package be.kuleuven;

import be.kuleuven.Instances.*;
import be.kuleuven.Interfaces.*;

import java.net.*;
import java.rmi.*;
import java.util.*;

public class Client {

    public BulletinBoardInterface bulletinBoardInterface;
    private final UserInterface userInterface;
    private String clientName;
    private final List<Entry> entries_AB;
    private final List<Entry> entries_BA;

    public Client(String clientName, UserInterface userInterface){
        this.clientName = clientName;
        this.userInterface = userInterface;
        this.entries_AB = new ArrayList<>();
        this.entries_BA = new ArrayList<>();
    }

    public void connectToRMIServer() throws RemoteException {
        try {
            bulletinBoardInterface = (BulletinBoardInterface) Naming.lookup("rmi://localhost/chat/");
            System.out.println("Verbonden met de RMI-server.\n");
        } catch (RemoteException | NotBoundException | MalformedURLException e) {
            System.err.println("Fout bij verbinden met de RMI-server:");
            e.printStackTrace();
        }
    }

    public BulletinEntry getBulletEntry_AB_from(String name) {
        return entries_AB.stream()
                .filter(entry -> entry.getName().equals(name))
                .findFirst()
                .map(Entry::getBulletinEntry)
                .orElse(null);
    }

    public BulletinEntry getBulletinEntry_BA_from(String name) {
        return entries_BA.stream()
                .filter(entry -> entry.getName().equals(name))
                .findFirst()
                .map(Entry::getBulletinEntry)
                .orElse(null);
    }

    public UserInterface getUserInterface() {
        return userInterface;
    }

    public BulletinBoardInterface getBulletinBoardInterface() {
        return bulletinBoardInterface;
    }

    public String getName() {
        return clientName;
    }

}
