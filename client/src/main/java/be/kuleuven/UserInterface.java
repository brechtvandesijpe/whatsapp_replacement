package be.kuleuven;

import javax.swing.*;

public class UserInterface extends JFrame{
    // attributes
    private String clientName;


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

    public UserInterface(String title) {
        super(title);
        setSize(1280,720);
        setContentPane(panel);
        setAllComponentsVisible();

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
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame userInterface = new UserInterface("Whatsapp Replacement DS");
            userInterface.setVisible(true);
        });
    }

    // ***************** ButtonsClicks ************************

    public void handleJoinButtonClick() {
        clientName = usernameTextField.getText().toLowerCase();
        if(!clientName.isEmpty()) {
            clearUsernameTextField();
            setButtonsEnabled(false, true, true, true, true);
        }

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
