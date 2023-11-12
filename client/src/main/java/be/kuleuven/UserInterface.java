package be.kuleuven;

import be.kuleuven.Interfaces.BulletinBoardInterface;

import javax.swing.*;
import java.rmi.RemoteException;

public class UserInterface extends JFrame{
    // attributes
    private String clientName;
    private Client client;
    private BulletinBoardInterface bulletinBoardInterface;
    // javax.swing
    private JPanel panel;
    private JTextField userListTextField;
    private JTextField usernameTextField;
    private JTextArea chatArea;
    private JList userList;
    private JButton joinButton;
    private JButton bumpButton;
    private JButton bumpBackButton;
    private JButton leaveButton;
    private JButton sendMessageButton;
    private JLabel statusLabel;

    public UserInterface(String title) throws RemoteException {
        super(title);
        setSize(1280,720);
        setContentPane(panel);
        setAllComponentsVisible();
        addButtonClickListeners();
        client = new Client(clientName, this, bulletinBoardInterface);
    }

    public void setAllComponentsVisible() {
        userListTextField.setVisible(true);
        usernameTextField.setVisible(true);
        chatArea.setVisible(true);
        userList.setVisible(true);
        joinButton.setVisible(true);
        bumpButton.setVisible(true);
        bumpBackButton.setVisible(true);
        leaveButton.setVisible(true);
        sendMessageButton.setVisible(true);
        statusLabel.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame userInterface;
            try {
                userInterface = new UserInterface("Whatsapp Replacement DS");
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
            userInterface.setVisible(true);
        });
    }

    public void addButtonClickListeners() {
        joinButton.addActionListener(e -> {
                    try {
                        handleJoinButtonClick();
                    } catch (RemoteException ex) {
                        throw new RuntimeException(ex);
                    }
                }
        );
    }
    // ***************** ButtonsClicks ************************

    public void handleJoinButtonClick() throws RemoteException {
        clientName = usernameTextField.getText().toLowerCase();
        if(!clientName.isEmpty()) {
            clearUsernameTextField();
            setButtonsEnabled(false, true, true, true, true);
            start(clientName);
            statusLabel.setText("You successfully join as " + clientName);
        }else{
            showErrorDialog("You forgot to fill in your clientName");
            statusLabel.setText("You forgot to fill in your name!");
        }
    }

    public void start(String clientName) throws RemoteException {
        client = new Client(clientName, this, client.getBulletinBoardInterface());
        client.connectToRMIServer();
        System.err.println("Client " + clientName + " is ready to chat.");
    }

    public void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(this, message, "Oopsie Woopsie! Something failed!", JOptionPane.ERROR_MESSAGE);
    }

    public void clearUsernameTextField() {
        usernameTextField.setText("");
    }

    public void setButtonsEnabled(boolean joinButton, boolean leaveButton, boolean bumpButton, boolean bumpBackButton, boolean sendMessageButton) {
        this.joinButton.setEnabled(joinButton);
        this.leaveButton.setEnabled(leaveButton);
        this.bumpButton.setEnabled(bumpButton);
        this.bumpBackButton.setEnabled(bumpBackButton);
        this.sendMessageButton.setEnabled(sendMessageButton);
    }

}
