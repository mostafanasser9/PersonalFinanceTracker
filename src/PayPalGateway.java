// Mock PayPal Gateway with its own specific API
public class PayPalGateway {
    public void sendPayment(String payPalEmail, double totalAmount) {
        // Simulate PayPal payment processing
        System.out.println("Processing PayPal payment of $" + totalAmount + " for user " + payPalEmail);
        // Assume payment is successful for mock
    }
}
