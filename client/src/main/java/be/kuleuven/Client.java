package be.kuleuven;

import be.kuleuven.Interfaces.BulletinBoardInterface;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class Client {

    public BulletinBoardInterface bulletinBoardInterface;
    private final UserInterface userInterface;
    private String clientName;

    public Client(String clientName, UserInterface userInterface){
        this.clientName = clientName;
        this.userInterface = userInterface;
    }

    public void connectToRMIServer() throws RemoteException {
        try {
            bulletinBoardInterface = (BulletinBoardInterface) Naming.lookup("rmi://localhost/groupChat");
            System.out.println("Verbonden met de RMI-server.\n");
        } catch (RemoteException | NotBoundException | MalformedURLException e) {
            System.err.println("Fout bij verbinden met de RMI-server:");
            e.printStackTrace();
        }
    }

}
