package be.kuleuven;

import be.kuleuven.Instances.ContactInfo;
import be.kuleuven.Interfaces.*;
import be.kuleuven.Managers.SecurityManager;
import be.kuleuven.Managers.StateManager;
import be.kuleuven.MessageHandling.PeriodicMessageFetcher;
import be.kuleuven.Util.*;

import javax.crypto.*;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.io.File;
import java.io.IOException;
import java.rmi.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

// Class representing the user interface for the chat application
public class UserInterface extends JFrame {
    // Attributes
    private String clientName;
    private PeriodicMessageFetcher messageFetcherTask;
    private AppState currentState = AppState.DEFAULT;
    private Client client;
    private BulletinBoardInterface bulletinBoardInterface;
    private String bumpString;
    private String passphrase;
    private String passphrase_BB;

    private int boxNumber_AB;
    private int boxNumber_BA;
    private byte[] tag_AB;
    private byte[] tag_BA;
    private SecretKey secretKey_AB;
    private SecretKey secretKey_BA;
    private final DefaultListModel<String> contactListModel;
    private final StateManager stateManager;

    // javax.swing components
    private JPanel panel;
    private JTextField messageTextField;
    private JTextField usernameTextField;
    private JTextArea chatArea;
    private JList<String> userList;
    private JButton joinButton;
    private JButton bumpButton;
    private JButton bumpBackButton;
    private JButton leaveButton;
    private JButton sendMessageButton;
    private JLabel statusLabel;
    private JLabel username_header;
    private JPanel panel2;
    private JScrollPane jscrollpane;
    private JButton restoreButton;
    private String passPhrase;
    private String contactName;

    public UserInterface(String title) throws RemoteException {
        super(title);
        contactListModel = new DefaultListModel<>();
        userList = new JList<>(contactListModel);
        userList.addListSelectionListener(this::handleListSelectionChange);
        jscrollpane.setViewportView(userList);
        setSize(1280,720);
        setContentPane(panel);
        setAllComponentsVisible();
        setButtonsEnabled(true, false, false, false, false,  false);
        addButtonClickListeners();
        client = new Client(clientName, this, bulletinBoardInterface);
        messageTextField.requestFocus();
        stateManager = new StateManager(this);
        this.getRootPane().setDefaultButton(joinButton);
    }

    // Method to set all graphical components visible
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
        restoreButton.setVisible(true);
        statusLabel.setVisible(true);
        panel2.setVisible(true);
    }

    // Main method to start the user interface
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
        bumpButton.addActionListener(e -> handleBumpButtonClick());

        sendMessageButton.addActionListener( e -> {
            try {
                handleSendMessageButtonClick();
            } catch (IllegalBlockSizeException | NoSuchPaddingException | BadPaddingException |
                     NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException | IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        bumpBackButton.addActionListener(e -> handleBumpBackButtonClick());

        restoreButton.addActionListener(e -> {
            try {
                handleRestoreButtonClick();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        leaveButton.addActionListener(e -> {
            handleLeaveButtonClick();
        });
    }

    // ***************** ButtonsClicks ************************

    public void handleJoinButtonClick() throws RemoteException {
        clientName = usernameTextField.getText().toLowerCase();
        if(!clientName.isEmpty()) {
            try {
                start(clientName);
                username_header.setText("Client of " + clientName);
                clearUsernameTextField();
                setButtonsEnabled(false, true, true, true, true,  true);
                usernameTextField.setEnabled(false);
                statusLabel.setText("You successfully joined as " + clientName);
                messageFetcherTask = new PeriodicMessageFetcher(client, this);
                messageFetcherTask.startPeriodicMessageFetching();
                this.getRootPane().setDefaultButton(sendMessageButton);
            } catch(ConnectException e) {
                showErrorDialog(e.getMessage());
            }
        } else {
            showErrorDialog("You forgot to fill in your clientName");
            statusLabel.setText("You forgot to fill in your name!");
        }
    }

    public void handleBumpButtonClick() {
        userList.clearSelection();
        generateBumpString();
        clearChatArea();
        showInChatArea("Here's the unique bump string for you and your contact: [" + bumpString + "], enter a chosen passphrase to initiate the contact" + "\n");
        statusLabel.setText("Bump Action");
        System.out.println("Client " + clientName + " started a bump action.");
        currentState = AppState.PASSPHRASE;
    }

    public void handleBumpBackButtonClick() {
        userList.clearSelection();
        clearChatArea();
        showInChatArea("Fill in the bump string of you and your contact:");
        statusLabel.setText("Bump Back Action");
        System.out.println("Client " + clientName + " started a bump-back action.");
        currentState = AppState.BUMPSTRING_BB;
    }

    public void saveState() throws IOException {
        String fileName = "backup_" + client.getName() + ".txt";
        stateManager.saveState(new File(fileName), client.getEntries_AB(), client.getEntries_BA(), client.getHistoryManager().getMessageHistory());
    }

    public void handleRestoreButtonClick() throws IOException {
        String fileName = "backup_" + client.getName() + ".txt";
        stateManager.restoreState(new File(fileName), client.getEntries_AB(), client.getEntries_BA(), client.getHistoryManager().getMessageHistory());
        setButtonsEnabled(false, true, true, true, true,  false);
    }

    public void handleSendMessageButtonClick() throws IOException, IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException {
        if(!messageTextField.getText().isEmpty()) {
            switch (currentState) {
                case PASSPHRASE:
                    handleSMB_passphrase();
                    break;
                case CONTACTNAME:
                    handleSMB_contactName();
                    break;
                case PASSPHRASE_BB:
                    handleSMB_passphrase_bb();
                    break;
                case BUMPSTRING_BB:
                    handleSMB_bumpstring_bb();
                case DEFAULT:
                    handleSMB_default();
                    break;
            }
        }
    }

    // Initially (at the same time they also exchange the necessary cryptographic
    // keys) they agree on a tag and the index of the first cell to use.
    public void handleSMB_passphrase() throws RemoteException {
        passphrase = messageTextField.getText();
        setInitialBoxNumbers(passphrase, true);
        setInitialTags(true);
        setInitialKeys(true);
        clearMessageTextField();
        showInChatArea("With what name do you want to save that client in your contactlist?");
        statusLabel.setText("Filled In passphrase as B");
        currentState = AppState.CONTACTNAME;
    }

    public void handleSMB_passphrase_bb() throws RemoteException {
        passphrase_BB = messageTextField.getText();
        clearMessageTextField();
        setInitialBoxNumbers(passphrase_BB, false);
        setInitialTags(false);
        setInitialKeys(false);
        showInChatArea("With what name do you want to save that client in your contactlist?");
        statusLabel.setText("Filled In passphrase as BB");
        currentState = AppState.CONTACTNAME;
    }

    public void handleSMB_bumpstring_bb() {
        clearChatArea();
        userList.clearSelection();
        clearMessageTextField();
        showInChatArea("Fill in the passphrase of you and your contact");;
        currentState = AppState.PASSPHRASE_BB;
    }

    public void handleSMB_contactName() {
        String contactName = messageTextField.getText();
        contactListModel.addElement(contactName);
        System.out.println("Nieuw ContactListModel: " + contactListModel);
        userList.setSelectedIndex(contactListModel.indexOf(contactName));
        clearChatArea();
        clearMessageTextField();
        client.addContact(new ContactInfo(contactName, boxNumber_AB, boxNumber_BA, tag_AB, tag_BA, secretKey_AB, secretKey_BA));
        resetContactInfo();
        currentState = AppState.DEFAULT;
        statusLabel.setText("Filled In Contactname");
    }

    public void handleSMB_default() throws IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, IOException, InvalidKeyException {
        int index = userList.getSelectedIndex();
        if (index != -1) {
            String message = messageTextField.getText();
            sendMessage(message, contactListModel.getElementAt(index));
        }
        clearMessageTextField();
        // Vanaf nu kan je niet meer restoren
        setButtonsEnabled(false, true, true, true, true,  false);
    }

    // *************** HELPER METHODS **************************

    public void start(String clientName) throws ConnectException, RemoteException {
        client = new Client(clientName, this, client.getBulletinBoard());
        client.connectToRMIServer();
        System.err.println("Client " + clientName + " is ready to chat.");
    }

    public void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(this, message, "Oopsie Woopsie! Something failed!", JOptionPane.ERROR_MESSAGE);
    }

    public void showBumpDialog() {
        this.passPhrase = JOptionPane.showInputDialog("Your bump string is: " + bumpString + "\n Please enter a passphrase of your choice: ");
    }

    public void showBBSDialog() {
        this.passPhrase = JOptionPane.showInputDialog("Please enter the bump string: ");
    }

    public void showBumpPassDialog() {
        this.passPhrase = JOptionPane.showInputDialog("Please enter the passphrase: ");
    }

    public void showContactDialog() {
        this.contactName = JOptionPane.showInputDialog("Please enter the contact's name: ");
    }

    public void clearUsernameTextField() {
        usernameTextField.setText("");
    }

    public void clearChatArea() {
        chatArea.setText("");
    }

    public void clearMessageTextField() {
        messageTextField.setText("");
    }

    public void showInChatArea(String message) {
        chatArea.append(message);
        chatArea.append("\n");
    }

    public void setButtonsEnabled(boolean joinButton, boolean leaveButton, boolean bumpButton, boolean bumpBackButton, boolean sendMessageButton, boolean restoreButton) {
        this.joinButton.setEnabled(joinButton);
        this.leaveButton.setEnabled(leaveButton);
        this.bumpButton.setEnabled(bumpButton);
        this.bumpBackButton.setEnabled(bumpBackButton);
        this.sendMessageButton.setEnabled(sendMessageButton);
        this.restoreButton.setEnabled(restoreButton);
    }

    public void generateBumpString() {
        bumpString = RandomStringGenerator.generateRandomString(10);
    }

    public void setInitialBoxNumbers(String passphrase, boolean isInitiator) throws RemoteException {
        // De eerste boxnumbers worden afgeleid uit common parameter passphrase, alle volgende boxenumbers zijn random
        if (isInitiator) {
            boxNumber_AB = Math.abs(RandomStringGenerator.deriveIntFromPasshrase(passphrase)) % client.getBulletinBoard().getAmountOfMailboxes();
            boxNumber_BA = Math.abs(RandomStringGenerator.deriveIntFromPasshrase(new StringBuilder(passphrase).reverse().toString())) % client.getBulletinBoard().getAmountOfMailboxes();
            System.out.println("AB: " + boxNumber_AB + ", BA: " + boxNumber_BA);
        } else {
            boxNumber_BA = Math.abs(RandomStringGenerator.deriveIntFromPasshrase(passphrase_BB)) % client.getBulletinBoard().getAmountOfMailboxes();
            boxNumber_AB = Math.abs(RandomStringGenerator.deriveIntFromPasshrase(new StringBuilder(passphrase_BB).reverse().toString())) % client.getBulletinBoard().getAmountOfMailboxes();
            System.out.println("AB: " + boxNumber_AB + ", BA: " + boxNumber_BA);
        }
    }

    public void setInitialTags(boolean isInitiator) {
        // De eerste tags worden afgeleid uit common parameter passphrase, alle volgende tags zijn random
        if (isInitiator) {
            tag_AB = RandomStringGenerator.deriveBytesFromPassphrase(passphrase);
            tag_BA = RandomStringGenerator.deriveBytesFromPassphrase(new StringBuilder(passphrase).reverse().toString());
            System.out.println("tag_AB: " + new String(tag_AB) + ", " + new String(tag_BA));
        } else {
            tag_BA = RandomStringGenerator.deriveBytesFromPassphrase(passphrase_BB);
            tag_AB = RandomStringGenerator.deriveBytesFromPassphrase(new StringBuilder(passphrase_BB).reverse().toString());
            System.out.println("tag_AB: " + new String(tag_AB) + ", " + new String(tag_BA));
        }
    }

    public void setInitialKeys(boolean isInitiator) {
        // De tags worden als salt meegegeven, maar kan eender wat zijn
        if (isInitiator) {
            System.out.println("Passphrase: " + passphrase);
            secretKey_AB = SecurityManager.getSymmetricKey(passphrase, tag_AB);
            secretKey_BA = SecurityManager.getSymmetricKey(passphrase, tag_BA);
        } else {
            System.out.println("Passphrase_BB: " + passphrase_BB);
            secretKey_AB = SecurityManager.getSymmetricKey(passphrase_BB, tag_AB);
            secretKey_BA = SecurityManager.getSymmetricKey(passphrase_BB, tag_BA);
        }
    }

    public void resetContactInfo() {
        secretKey_AB = null;
        secretKey_BA = null;
        boxNumber_AB = Integer.MIN_VALUE;
        boxNumber_BA = Integer.MIN_VALUE;
        tag_AB = null;
        tag_BA = null;
    }

    public void handleListSelectionChange(ListSelectionEvent listSelectionEvent) {
        System.out.println("Userlist: " + userList.getSize());
        if (!userList.getValueIsAdjusting()) return;
        clearChatArea();
        System.out.println("contactListModel" + contactListModel);
        int maxSelectionIndex = userList.getMaxSelectionIndex();
        if (maxSelectionIndex != -1) {
            client.getHistoryManager().getMessageHistory().get(contactListModel.getElementAt(maxSelectionIndex)).forEach(message -> chatArea.append(message));
            System.out.println("Client " + clientName + " its contactList changes.");
        } else {
            System.err.println("SelectionIndex was -1");
        }
    }

    public String getContactAtIndex(int ind){
        return contactListModel.elementAt(ind);
    }

    public int[] getSelectedContacts(){
        return userList.getSelectedIndices();
    }

    public void disconnect() {
        messageFetcherTask.stopTimer();
        System.exit(-1);
        System.out.println("Client " + clientName + " successfully disconnected.");
    }

    private void sendMessage(String message, String contactName) throws RemoteException, BadPaddingException, InvalidKeyException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException {
        client.sendMessageTo(contactName, message);
    }

    public void handleLeaveButtonClick() {
        try {
            client.leave();
        } catch(RemoteException e) {
            e.printStackTrace();
        }
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

    public PeriodicMessageFetcher getMessageFetcherTask() {
        return messageFetcherTask;
    }

    public void setMessageFetcherTask(PeriodicMessageFetcher messageFetcherTask) {
        this.messageFetcherTask = messageFetcherTask;
    }

    public AppState getCurrentState() {
        return currentState;
    }

    public void setCurrentState(AppState currentState) {
        this.currentState = currentState;
    }

    public DefaultListModel<String> getContactListModel() {
        return contactListModel;
    }
}
