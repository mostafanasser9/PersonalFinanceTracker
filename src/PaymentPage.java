import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PaymentPage extends JFrame {
    private JPanel mainPanel;
    private JButton proceedButton;
    private JPasswordField cardNumberField; // Used for CVV / PayPal Password
    private JTextField cardOwnerField;    // Used for Card Number / PayPal Email
    private JRadioButton payPalRadioButton;
    private JRadioButton creditCardRadioButton;
    private JPanel Payment;
    private JLabel cardOwnerLabel; // Label for the first input field
    private JLabel cardNumberLabel; // Label for the second input field
    private String currentUserEmail;
    private UserContext userContext; // Added UserContext

    public PaymentPage(String userEmail, UserContext userContext) { // Modified constructor
        this.currentUserEmail = userEmail;
        this.userContext = userContext; // Store UserContext
        setTitle("Payment Page - " + currentUserEmail);
        setContentPane(Payment); // Assuming 'Payment' is the main JPanel from your .form file
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);
        setResizable(false);

        ButtonGroup paymentMethodGroup = new ButtonGroup();
        paymentMethodGroup.add(payPalRadioButton);
        paymentMethodGroup.add(creditCardRadioButton);
        payPalRadioButton.setSelected(true); // Default selection
        updateLabels(); // Initial label setup based on default selection

        // Add action listeners to radio buttons to update labels dynamically
        payPalRadioButton.addActionListener(e -> updateLabels());
        creditCardRadioButton.addActionListener(e -> updateLabels());

        proceedButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                double upgradeAmount = 10.0; // Example upgrade amount
                Command commandToExecute = null;

                if (payPalRadioButton.isSelected()) {
                    PaymentProcessor paymentProcessor = new PayPalAdapter(new PayPalGateway());
                    String payPalEmail = cardOwnerField.getText().trim();
                    String payPalPassword = new String(cardNumberField.getPassword());

                    if (payPalEmail.isEmpty()) {
                        JOptionPane.showMessageDialog(PaymentPage.this, "PayPal email cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    if (!payPalEmail.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                        JOptionPane.showMessageDialog(PaymentPage.this, "Invalid PayPal email format.", "Input Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    if (payPalPassword.isEmpty()) {
                        JOptionPane.showMessageDialog(PaymentPage.this, "PayPal password cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    commandToExecute = new PayPalPaymentCommand(paymentProcessor, upgradeAmount, payPalEmail);
                } else if (creditCardRadioButton.isSelected()) {
                    PaymentProcessor paymentProcessor = new CreditCardAdapter(new CreditCardGateway());
                    String actualCardNumber = cardOwnerField.getText().trim(); // cardOwnerField for Card Number
                    String cvv = new String(cardNumberField.getPassword()); // cardNumberField for CVV
                    String expiryDate = "12/25"; // Placeholder, ideally from another UI field

                    if (actualCardNumber.isEmpty()) {
                        JOptionPane.showMessageDialog(PaymentPage.this, "Card number cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    if (!actualCardNumber.matches("^\\d{16}$")) {
                        JOptionPane.showMessageDialog(PaymentPage.this, "Invalid card number format (should be 16 digits).", "Input Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    if (cvv.isEmpty()) {
                        JOptionPane.showMessageDialog(PaymentPage.this, "CVV cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    if (!cvv.matches("^\\d{3,4}$")) {
                        JOptionPane.showMessageDialog(PaymentPage.this, "Invalid CVV format (should be 3 or 4 digits).", "Input Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    commandToExecute = new CreditCardPaymentCommand(paymentProcessor, upgradeAmount, currentUserEmail, actualCardNumber, expiryDate, cvv);
                } else {
                     JOptionPane.showMessageDialog(PaymentPage.this, "Please select a payment method.", "Payment Error", JOptionPane.WARNING_MESSAGE);
                     return;
                }

                if (commandToExecute != null) {
                    if (commandToExecute.execute()) {
                        handlePaymentSuccess();
                    } else {
                        if (payPalRadioButton.isSelected()) {
                            JOptionPane.showMessageDialog(PaymentPage.this, "PayPal Payment Failed.", "Payment Error", JOptionPane.ERROR_MESSAGE);
                        } else if (creditCardRadioButton.isSelected()) {
                            JOptionPane.showMessageDialog(PaymentPage.this, "Credit Card Payment Failed.", "Payment Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        });
        setVisible(true);
    }

    private void updateLabels() {
        if (payPalRadioButton.isSelected()) {
            if (cardOwnerLabel != null) cardOwnerLabel.setText("PayPal Email:");
            if (cardNumberLabel != null) cardNumberLabel.setText("PayPal Password:");
            if (cardNumberField != null) cardNumberField.setEchoChar('*'); // Mask password
        } else if (creditCardRadioButton.isSelected()) {
            if (cardOwnerLabel != null) cardOwnerLabel.setText("Card Number:");
            if (cardNumberLabel != null) cardNumberLabel.setText("CVV:");
            if (cardNumberField != null) cardNumberField.setEchoChar('*'); // Mask CVV
        }
    }

    private void handlePaymentSuccess() {
        UserManager.getInstance().setUserRole(currentUserEmail, "premium");
        if (this.userContext != null) {
            this.userContext.refreshState(); // Notify UserContext to update its state
        }
        JOptionPane.showMessageDialog(this, "Payment successful! You are now a premium user.", "Payment Success", JOptionPane.INFORMATION_MESSAGE);
        dispose(); // Close payment page
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PaymentPage("test@example.com", new UserContext("test@example.com", null)));
    }
}
