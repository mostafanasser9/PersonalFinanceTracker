import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PaymentPage extends JFrame {
    private JPanel mainPanel;
    private JButton proceedButton;
    private JPasswordField cardNumberField; // Your new field for CVV / PayPal Password
    private JTextField cardOwnerField;    // Your new field for Card Number / PayPal Email
    private JRadioButton payPalRadioButton;
    private JRadioButton creditCardRadioButton;
    private JPanel Payment;
    private String currentUserEmail;
    private PaymentProcessor paymentProcessor; // Use the adapter interface

    public PaymentPage(String userEmail) {
        this.currentUserEmail = userEmail;
        setTitle("Payment Page");
        setContentPane(Payment);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);
        setResizable(false);

        ButtonGroup paymentMethodGroup = new ButtonGroup();
        paymentMethodGroup.add(payPalRadioButton);
        paymentMethodGroup.add(creditCardRadioButton);
        payPalRadioButton.setSelected(true);

        proceedButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                double upgradeAmount = 10.0; // Example upgrade amount

                if (payPalRadioButton.isSelected()) {
                    paymentProcessor = new PayPalAdapter(new PayPalGateway());
                    String payPalEmail = cardOwnerField.getText(); // Use cardOwnerField for PayPal email
                    // String payPalPassword = new String(cardNumberField.getPassword()); // If PayPal needs a password

                    if (payPalEmail.isEmpty()) {
                        // JOptionPane.showMessageDialog(PaymentPage.this, "PayPal email cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
                        // return;
                        payPalEmail = currentUserEmail; // Default to current user's email if field is empty
                    }
                    // For this mock, the PayPalGateway doesn't use a password, so cardNumberField is not directly used here.
                    // If it were, you'd pass payPalPassword or similar.
                    if (paymentProcessor.processPayment(upgradeAmount, payPalEmail)) {
                        upgradeUser();
                    } else {
                        JOptionPane.showMessageDialog(PaymentPage.this, "PayPal Payment Failed.", "Payment Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else if (creditCardRadioButton.isSelected()) {
                    paymentProcessor = new CreditCardAdapter(new CreditCardGateway());
                    String ccNumber = cardOwnerField.getText(); // Use cardOwnerField for Card Number
                    String cvv = new String(cardNumberField.getPassword()); // Use cardNumberField (JPasswordField) for CVV

                    // You'll need more fields for expiry date in a real scenario.
                    String expiryDate = "12/25"; // Placeholder, ideally from another UI field

                    if (ccNumber.isEmpty() || cvv.isEmpty()) {
                        JOptionPane.showMessageDialog(PaymentPage.this, "Card number and CVV cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    if (paymentProcessor.processPayment(upgradeAmount, currentUserEmail, ccNumber, expiryDate, cvv)) {
                        upgradeUser();
                    } else {
                        JOptionPane.showMessageDialog(PaymentPage.this, "Credit Card Payment Failed.", "Payment Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                     JOptionPane.showMessageDialog(PaymentPage.this, "Please select a payment method.", "Payment Error", JOptionPane.WARNING_MESSAGE);
                     return;
                }
            }
        });
        setVisible(true);
    }

    private void upgradeUser() {
        UserManager userManager = UserManager.getInstance();
        userManager.setUserRole(currentUserEmail, "premium");
        HistoryLogger.getInstance().addLog("User " + currentUserEmail + " upgraded to premium.");
        JOptionPane.showMessageDialog(PaymentPage.this, "Upgraded To Premium Successfully");
        dispose();
    }

    public static void main(String[] args) {
        // Example usage: Pass a dummy email or get it from a logged-in session
        SwingUtilities.invokeLater(() -> new PaymentPage("test@example.com"));
    }
}
