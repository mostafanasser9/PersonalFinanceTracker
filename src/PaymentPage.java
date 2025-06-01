import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PaymentPage extends JFrame {
    private JPanel mainPanel;
    private JButton proceedButton;
    private JPasswordField passwordField1;
    private JTextField textField1;
    private JRadioButton payPalRadioButton;
    private JRadioButton cashRadioButton;
    private JPanel Payment;
    private String currentUserEmail; // To store the email of the user upgrading

    public PaymentPage(String userEmail) { // Modified constructor
        this.currentUserEmail = userEmail; // Store the user's email
        setTitle("Payment Page");
        setContentPane(Payment);  // This comes from the .form file
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);

        proceedButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Upgrade user to premium
                UserManager userManager = UserManager.getInstance();
                userManager.setUserRole(currentUserEmail, "premium"); // Use the stored email

                JOptionPane.showMessageDialog(PaymentPage.this, "Upgraded To Premium Successfully");
                // Optionally, close the payment page and refresh the dashboard or relevant UI
                dispose(); 
            }
        });
    }

    public static void main(String[] args) {
        // Example usage: Pass a dummy email or get it from a logged-in session
        SwingUtilities.invokeLater(() -> new PaymentPage("test@example.com"));
    }
}
