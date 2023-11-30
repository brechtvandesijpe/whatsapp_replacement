package be.kuleuven;

import javax.crypto.*;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.io.IOException;
import java.rmi.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import be.kuleuven.connection.RandomStringGenerator;
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
    private final DefaultListModel<String> contactListModel;
    private final Client client;

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
        System.out.println("Hello world");
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

    public void clearUsernameTextField() {
        usernameTextField.setText("");
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
        clearUsernameTextField();
        setButtonsEnabled(false, true, true, true, true,  true);
        statusLabel.setText("You successfully joined as " + username);
    }

    public void addContact(String name) {
        System.out.println("add contact " + name);

        contactListModel.addElement(name);
        userList.setSelectedValue(name, false);
    }

    public String getSelectedContact(){
        return userList.getSelectedValue();
    }

    public void update(Chat chat) {
        if (getSelectedContact() == chat.getName()) {
            chatArea.setText(chat.toString());
        }
    }
}