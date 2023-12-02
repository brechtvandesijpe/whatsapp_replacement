package be.kuleuven;

import javax.crypto.*;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import be.kuleuven.connection.RandomStringGenerator;
import be.kuleuven.connection.Client;
import be.kuleuven.model.Chat;
import org.json.JSONArray;
import org.json.JSONObject;

// Class representing the user interface for the chat application
public class UserInterface extends JFrame {
    private JPanel panel;
    private JTextField messageTextField;
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
    private JButton addUserToChatButton;
    private DefaultListModel<String> contactListModel;
    private final Client client;

    public UserInterface(String title) {
        super(title);

        String username = JOptionPane.showInputDialog("Please fill in your usename: ");
        try {
            client = Client.createInstance(username, this);
        } catch(RemoteException ex) {
            throw new RuntimeException("RemoteException when creating new client");
        }

        contactListModel = new DefaultListModel<>();
        userList = new JList<>(contactListModel);
        userList.addListSelectionListener(this::handleListSelectionChange);
        username_header.setText("Welcome " + username + "!");
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
            handleRestoreButtonClick();
        });

        leaveButton.addActionListener(e -> {
            handleLeaveButtonClick();
        });

        messageTextField.requestFocus();
        this.getRootPane().setDefaultButton(joinButton);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void handleRestoreButtonClick() {
        client.restore();
    }

    public static void main(String[] args) {
        new UserInterface("Whatsapp replacement");
    }

    // Method to set all graphical components visible
    public void setAllComponentsVisible() {
        messageTextField.setVisible(true);
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

    public void handleJoinButtonClick() throws RemoteException {
        client.join();
    }

    public void handleBumpButtonClick() {
        String bumpstring = RandomStringGenerator.generateRandomString(10);

        JTextField passphraseField = new JTextField(20);
        passphraseField.setText(bumpstring);
        passphraseField.setEditable(false);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JLabel("Your bumpstring is:"));
        panel.add(passphraseField);
        panel.add(new JLabel("Please enter the passphrase of your choice: "));

        String passphrase = "";
        while (passphrase.isEmpty()) {
            passphrase = JOptionPane.showInputDialog(null, panel, "Bump", JOptionPane.PLAIN_MESSAGE);
            if (passphrase.isEmpty()) {
                showErrorDialog("Your passphrase cannot be empty!");
            }
        }

        client.bump(bumpstring, passphrase);
    }

    public void handleBumpBackButtonClick() {
        String bumpstring = "";
        while (bumpstring.isEmpty()) {
            bumpstring = JOptionPane.showInputDialog("Please enter the bumpstring: ");
            if (bumpstring.isEmpty()) {
                showErrorDialog("Your passphrase cannot be empty!");
            }
        }

        String passphrase = "";
        while (passphrase.isEmpty()) {
            passphrase = JOptionPane.showInputDialog("Please enter the passphrase: ");
            if (passphrase.isEmpty()) {
                showErrorDialog("Your passphrase cannot be empty!");
            }
        }

        client.bumpBack(bumpstring, passphrase);
    }

    public void handleLeaveButtonClick() {
        client.leave();
    }

    public void handleSendMessageButtonClick() throws IOException, IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException {
        client.sendMessage(messageTextField.getText(), getSelectedContact());
    }

    public void handleListSelectionChange(ListSelectionEvent listSelectionEvent) {
        if (!userList.getValueIsAdjusting()) return;
        clearChatArea();
        chatArea.append(client.getChat(getSelectedContact()).toString());
    }

    public void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void clearChatArea() {
        chatArea.setText("");
    }

    public void setButtonsEnabled(boolean joinButton, boolean leaveButton, boolean bumpButton, boolean bumpBackButton, boolean sendMessageButton, boolean restoreButton) {
        this.joinButton.setEnabled(joinButton);
        this.leaveButton.setEnabled(leaveButton);
        this.bumpButton.setEnabled(bumpButton);
        this.bumpBackButton.setEnabled(bumpBackButton);
        this.sendMessageButton.setEnabled(sendMessageButton);
        this.restoreButton.setEnabled(restoreButton);
    }

    public void initiate(String username) {
        setButtonsEnabled(false, true, true, true, true,  true);
        statusLabel.setText("You successfully joined as " + username);
    }

    public int addContact(String name) {
        contactListModel.addElement(name);
        userList.setSelectedValue(name, false);
        return contactListModel.getSize() - 1;
    }

    public int getSelectedContact(){
        System.out.println("returned selected index = " + userList.getSelectedIndex());
        return userList.getSelectedIndex();
    }

    public void update(Chat chat) {
        if (client.isChat(getSelectedContact(), chat)) {
            chatArea.setText(chat.toString());
        }
    }

    public void removeSelectedContact() {
        userList.remove(getSelectedContact());
    }

    public JSONArray getJSONContacts() {
        JSONArray output = new JSONArray();

        for (int i = 0; i < contactListModel.getSize(); i++) {
            output.put(contactListModel.get(i));
        }

        return output;
    }

    public void setContactListModel(JSONArray data) {
        contactListModel = new DefaultListModel<>();
        for(Object o : data) {
            contactListModel.addElement((String) o);
        }
        userList.setModel(contactListModel);
    }
}