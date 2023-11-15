package be.kuleuven;

import be.kuleuven.Interfaces.*;
import be.kuleuven.Managers.SecurityManager;
import be.kuleuven.Util.*;

import javax.crypto.*;
import javax.swing.*;
import java.rmi.*;

public class UserInterface extends JFrame{
    // attributes
    private String clientName;
    private AppState currentState = AppState.DEFAULT;
    private Client client;
    private BulletinBoardInterface bulletinBoardInterface;
    private String bumpString;
    private String passphrase;


    private int boxNumber_AB;
    private int boxNumber_BA;
    private byte[] tag_AB;
    private byte[] tagBA;
    private SecretKey secretKey_AB;
    private SecretKey secretKey_BA;

    // javax.swing
    private JPanel panel;
    private JTextField messageTextField;
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
        messageTextField.setVisible(true);
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
        bumpButton.addActionListener(e -> {
            handleBumpButtonClick();
        });

        sendMessageButton.addActionListener( e -> {
            handleSendMessageButtonClick();
        });
    }

    // ***************** ButtonsClicks ************************

    public void handleJoinButtonClick() throws RemoteException {
        clientName = usernameTextField.getText().toLowerCase();
        if(!clientName.isEmpty()) {
            clearUsernameTextField();
            setButtonsEnabled(false, true, true, true, true);
            start(clientName);
            statusLabel.setText("You successfully joined as " + clientName);
        }else{
            showErrorDialog("You forgot to fill in your clientName");
            statusLabel.setText("You forgot to fill in your name!");
        }
    }

    public void handleBumpButtonClick() {
        userList.clearSelection();
        generateBumpString();
        clearChatArea();
        showInChatArea("Here's the unique bump string for you and your friend: [" + bumpString + "], enter a chosen passphrase to initiate the contact" + "\n");
        statusLabel.setText("Bump Action");
        System.out.println("Client " + clientName + " started a bump action.");
        currentState = AppState.PASSPHRASE;
    }

    public void handleSendMessageButtonClick() {
        if(!messageTextField.getText().isEmpty()) {
            switch (currentState) {
                case PASSPHRASE:
                    handleSMB_passphrase();
                    break;
                case DEFAULT:
                    System.out.println("default");
                    break;
            }
        }
    }


    public void handleSMB_passphrase() {
        passphrase = messageTextField.getText();

        // TODO box, tag, key fixen
    }



    // *************** HELPER METHODS **************************

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

    public void clearChatArea() {
        chatArea.setText("");
    }

    public void showInChatArea(String message) {
        chatArea.append(message);
        chatArea.append("\n");
    }

    public void setButtonsEnabled(boolean joinButton, boolean leaveButton, boolean bumpButton, boolean bumpBackButton, boolean sendMessageButton) {
        this.joinButton.setEnabled(joinButton);
        this.leaveButton.setEnabled(leaveButton);
        this.bumpButton.setEnabled(bumpButton);
        this.bumpBackButton.setEnabled(bumpBackButton);
        this.sendMessageButton.setEnabled(sendMessageButton);
    }

    public void generateBumpString() {
        bumpString = RandomStringGenerator.generateRandomString(10);
    }



    // ***************** GETTERS *************************

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public BulletinBoardInterface getBulletinBoardInterface() {
        return bulletinBoardInterface;
    }

    public void setBulletinBoardInterface(BulletinBoardInterface bulletinBoardInterface) {
        this.bulletinBoardInterface = bulletinBoardInterface;
    }

    public JPanel getPanel() {
        return panel;
    }

    public JTextField getUserListTextField() {
        return messageTextField;
    }

    public JTextField getUsernameTextField() {
        return usernameTextField;
    }

    public JTextArea getChatArea() {
        return chatArea;
    }

    public JList getUserList() {
        return userList;
    }

    public JButton getJoinButton() {
        return joinButton;
    }

    public JButton getBumpButton() {
        return bumpButton;
    }

    public JButton getBumpBackButton() {
        return bumpBackButton;
    }

    public JButton getLeaveButton() {
        return leaveButton;
    }

    public JButton getSendMessageButton() {
        return sendMessageButton;
    }

    public JLabel getStatusLabel() {
        return statusLabel;
    }

    public String getBumpString() {
        return bumpString;
    }

    public void setBumpString(String bumpString) {
        this.bumpString = bumpString;
    }
}
