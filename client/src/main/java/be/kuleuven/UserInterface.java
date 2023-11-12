package be.kuleuven;

import javax.swing.*;

public class UserInterface {

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

    public UserInterface() {
        jFrame = new JFrame();
        jFrame.setVisible(true);
      
    }

    public static void main(String[] args) {
        UserInterface userInterface = new UserInterface();
    }
}
