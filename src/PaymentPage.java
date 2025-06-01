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
    private UserContext userContext; // Added UserContext

    public PaymentPage(String userEmail, UserContext userContext) { // Modified constructor
        this.currentUserEmail = userEmail;
        this.userContext = userContext; // Store UserContext
        setTitle("Payment Page - " + currentUserEmail);
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
                Command commandToExecute = null;

                if (payPalRadioButton.isSelected()) {
                    PaymentProcessor paymentProcessor = new PayPalAdapter(new PayPalGateway()); // Initialize the paymentProcessor
                    String payPalEmail = cardOwnerField.getText();
                    if (payPalEmail.isEmpty()) {
                        payPalEmail = currentUserEmail; // Default to current user's email if field is empty
                    }
                    commandToExecute = new PayPalPaymentCommand(paymentProcessor, upgradeAmount, payPalEmail);
                } else if (creditCardRadioButton.isSelected()) {
                    PaymentProcessor paymentProcessor = new CreditCardAdapter(new CreditCardGateway()); // Initialize the paymentProcessor
                    String ccNumber = cardOwnerField.getText();
                    String cvv = new String(cardNumberField.getPassword());
                    String expiryDate = "12/25"; // Placeholder, ideally from another UI field

                    if (ccNumber.isEmpty() || cvv.isEmpty()) {
                        JOptionPane.showMessageDialog(PaymentPage.this, "Card number and CVV cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    commandToExecute = new CreditCardPaymentCommand(paymentProcessor, upgradeAmount, currentUserEmail, ccNumber, expiryDate, cvv);
                } else {
                     JOptionPane.showMessageDialog(PaymentPage.this, "Please select a payment method.", "Payment Error", JOptionPane.WARNING_MESSAGE);
                     return;
                }

                if (commandToExecute != null) {
                    if (commandToExecute.execute()) {
                        handlePaymentSuccess();
                    } else {
                        // Show specific error message based on which payment method failed
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

    private void handlePaymentSuccess() {
        UserManager.getInstance().setUserRole(currentUserEmail, "premium");
        if (this.userContext != null) {
            this.userContext.refreshState(); // Notify UserContext to update its state
        }
        JOptionPane.showMessageDialog(this, "Payment successful! You are now a premium user.", "Payment Success", JOptionPane.INFORMATION_MESSAGE);
        dispose(); // Close payment page
    }

    public static void main(String[] args) {
        // Example usage: Pass a dummy email or get it from a logged-in session
        SwingUtilities.invokeLater(() -> new PaymentPage("test@example.com", new UserContext("test@example.com", null)));
    }
}
