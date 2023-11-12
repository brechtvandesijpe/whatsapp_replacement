package be.kuleuven;

import javax.swing.*;

public class UserInterface extends JFrame{

    private JFrame jFrame;
    private JPanel panel;
    private JTextField userListTextField;
    private JTextField usernameTextField;
    private JTextArea chatArea;
    private JList userList;
    private JButton joinButton;
    private JButton bumpButton;
    private JButton bumpBackButton;
    private JButton leaveButton;

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
    }



    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame userInterface = new UserInterface("Whatsapp Replacement DS");
            userInterface.setVisible(true);
        });
    }
}
