import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;

public class SignupPage {
    private JTextField textField1;
    private JPanel panel1;
    private JTextField textField2; // Assuming this is for username
    private JPasswordField passwordField1;
    private JButton SIGNUPButton;
    private JLabel loginLabel; // This should be bound in the .form file to the id "logLabel"
    private JTextArea enterYourDetailsToTextArea;
    private JFrame frame;
    private UserManager userManager; // Add UserManager instance

    public SignupPage() {
        // Initialize and set up the frame
        frame = new JFrame("Sign Up");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setPreferredSize(new Dimension(600, 400)); // Or match your .form size
        frame.setResizable(false);

        // The IntelliJ GUI designer should initialize panel1 and its components (like loginLabel)
        // before this constructor is effectively used if the .form file is correctly linked.
        // We add panel1 to the frame.
        frame.setContentPane(panel1); // Use setContentPane for the main panel

        frame.pack();
        frame.setLocationRelativeTo(null); // Center on screen
        frame.setVisible(true);

        userManager = UserManager.getInstance(); // Get UserManager instance

        // Ensure loginLabel is not null before adding a listener
        // This check is a safeguard; in a properly configured IntelliJ GUI form,
        // loginLabel should be initialized by the code generated from the .form file.
        if (loginLabel != null) {
            loginLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    new LoginPage(); // Create and show LoginPage
                    frame.dispose();   // Close the current SignupPage frame
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    loginLabel.setText("<html><u>Already have an account? Log In</u></html>");
                    loginLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    loginLabel.setText("Already have an account? Log In");
                    loginLabel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            });
        } else {
            // This case might occur if the .form file's binding for loginLabel (e.g., to "logLabel")
            // is missing or incorrect, or if the class is instantiated in a way that bypasses
            // IntelliJ's GUI initialization (e.g., manual instantiation without .form processing).
            // For robustness in environments where .form processing might not be perfect:
            // System.err.println("Warning: loginLabel is null in SignupPage constructor. Check .form binding for 'logLabel'.");
        }

        // Add ActionListener to SIGNUPButton
        if (SIGNUPButton != null) {
            SIGNUPButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String username = textField2.getText(); // Assuming textField2 is for username
                    String password = new String(passwordField1.getPassword());
                    // You might want to add validation for email (textField1) and password strength here

                    if (username.isEmpty() || password.isEmpty()) {
                        JOptionPane.showMessageDialog(frame, "Username and password cannot be empty.", "Signup Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    if (userManager.registerUser(username, password)) {
                        JOptionPane.showMessageDialog(frame, "Registration successful! Please login.", "Signup Success", JOptionPane.INFORMATION_MESSAGE);
                        // Open LoginPage
                        new LoginPage();
                        // Close current SignupPage frame
                        frame.dispose();
                    } else {
                        JOptionPane.showMessageDialog(frame, "Username already exists. Please choose another one.", "Signup Failed", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
        } else {
            // System.err.println("Warning: SIGNUPButton is null in SignupPage constructor. Check form binding.");
        }

        // TODO: Add ActionListener to SIGNUPButton for signup logic
        // SIGNUPButton.addActionListener(e -> { /* ... signup logic ... */ });
    }

    // It's good practice to have a main method for testing individual frames,
    // though not strictly necessary if launched from another part of the application.
    // public static void main(String[] args) {
    //     SwingUtilities.invokeLater(SignupPage::new);
    // }
}
