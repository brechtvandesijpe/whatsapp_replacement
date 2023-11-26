package be.kuleuven;

import javax.crypto.*;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.io.IOException;
import java.rmi.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Map;

import be.kuleuven.model.ChatMessage;
import be.kuleuven.connection.Client;
import be.kuleuven.model.Chat;

// Class representing the user interface for the chat application
public class UserInterface extends JFrame {
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
    private DefaultListModel<String> contactListModel;
    private Client client;
    private Map<String, Chat> messages;

    public UserInterface(String title) {
        super(title);

        String username = JOptionPane.showInputDialog("Please fill in your usename: ");
        try {
            client = new Client(username, this);
        } catch(RemoteException ex) {
            throw new RuntimeException("RemoteException when creating new client");
        }

        contactListModel = new DefaultListModel<>();
        userList = new JList<>(contactListModel);
        userList.addListSelectionListener(this::handleListSelectionChange);
        jscrollpane.setViewportView(userList);
        setSize(1280,720);
        setContentPane(panel);
        setAllComponentsVisible();
        setButtonsEnabled(true, false, false, false, false,  false);

        joinButton.addActionListener(e -> {
            try {
                handleJoinButtonClick();
            } catch (RemoteException ex) {
                throw new RuntimeException(ex);
            }
        });
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

        messageTextField.requestFocus();
        this.getRootPane().setDefaultButton(joinButton);
        usernameTextField.setEnabled(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    public static void main(String[] args) {
        new UserInterface("Whatsapp replacement");
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

    // ******************* UPDATE UI **************************

    public void setOwnUsername(String username) {
        username_header.setText("Welcome " + username + "!");
    }

    // ***************** ButtonsClicks ************************

    public void handleJoinButtonClick() throws RemoteException {
        client.join();
    }

    public void handleBumpButtonClick() {
        client.bump();
    }

    public void handleBumpBackButtonClick() {
        client.bumpBack();
    }

    public void handleLeaveButtonClick() {
        client.leave();
    }

    public void saveState() throws IOException {

    }

    public void handleRestoreButtonClick() throws IOException {

    }

    public void handleSendMessageButtonClick() throws IOException, IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException {
        int index = userList.getSelectedIndex();
        if (index != -1) {
            String message = messageTextField.getText();
            ChatMessage chatMessage = client.sendMessage(contactListModel.getElementAt(index), message);
            appendChatArea(chatMessage);
            clearMessageTextField();
        }
    }

    public void appendChatArea(ChatMessage chatMessage) {
        chatArea.append(chatMessage.toString() + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    public void handleListSelectionChange(ListSelectionEvent listSelectionEvent) {
        System.out.println("Userlist: " + userList.getSize());
        if (!userList.getValueIsAdjusting()) return;
        clearChatArea();
        System.out.println("contactListModel" + contactListModel);
        chatArea.append(messages.get(userList.getSelectedValue()).toString());
    }

    // HELPER METHODS

    public void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
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

    public void setStatus(String status) {
        statusLabel.setText(status);
    }

    public void initiate(String username) {
        clearUsernameTextField();
        setButtonsEnabled(false, true, true, true, true,  true);
        statusLabel.setText("You successfully joined as " + username);
    }

    public void addContact(String name) {
        contactListModel.addElement(name);
        userList.setSelectedValue(name, false);
    }

    public int[] getSelectedContacts(){
        return userList.getSelectedIndices();
    }

    public String getContactAtIndex(int i) {
        return contactListModel.elementAt(i);
    }

    public void removeContact(String name) {
        contactListModel.removeElement(name);
        userList.setSelectedValue(null, false);
    }
}
