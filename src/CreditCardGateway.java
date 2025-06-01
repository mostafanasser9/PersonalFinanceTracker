// Mock Credit Card Gateway with its own specific API
public class CreditCardGateway {
    public boolean chargeCard(String cardNumber, String expiryDate, String cvv, double amount) {
        // Simulate Credit Card payment processing
        System.out.println("Charging credit card " + cardNumber + " (Expiry: " + expiryDate + ", CVV: " + cvv + ") amount $" + amount);
        // Assume payment is successful for mock
        return true;
    }
}
