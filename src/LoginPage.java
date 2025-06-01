import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;

public class LoginPage {
    private JPanel panel1;
    private JTextField textField1;
    private JPasswordField passwordField1;
    private JButton LOGINButton;
    private JLabel registerLabel; // Added for the new label
    private JFrame frame;
    private UserManager userManager; // Add UserManager instance

    public LoginPage(){
        frame = new JFrame("Login Page");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(600, 400));
        frame.setResizable(false);
        // Add Panels
        frame.setContentPane(panel1); // Use setContentPane for the main panel
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        userManager = UserManager.getInstance(); // Get UserManager instance

        // Add ActionListener to LOGINButton
        if (LOGINButton != null) {
            LOGINButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String username = textField1.getText();
                    String password = new String(passwordField1.getPassword());
                    if (userManager.loginUser(username, password)) {
                        // Open Dashboard
                        new Dashboard();
                        // Close current LoginPage
                        frame.dispose();
                    } else {
                        JOptionPane.showMessageDialog(frame, "Invalid username or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
        } else {
            // System.err.println("Warning: LOGINButton is null in LoginPage constructor. Check form binding.");
        }

        // Add MouseListener to registerLabel
        registerLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Open SignupPage
                new SignupPage();
                // Close current LoginPage
                frame.dispose();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                registerLabel.setText("<html><u>Don't have an account? Register</u></html>");
                registerLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                registerLabel.setText("Don't have an account? Register");
                registerLabel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });
    }
}
