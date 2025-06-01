public class PayPalAdapter implements PaymentProcessor {
    private PayPalGateway payPalGateway;

    public PayPalAdapter(PayPalGateway payPalGateway) {
        this.payPalGateway = payPalGateway;
    }

    @Override
    public boolean processPayment(double amount, String userEmail, String... paymentDetails) {
        // For this adapter, userEmail is assumed to be the PayPal email.
        // paymentDetails could be used for other PayPal specific info if needed.
        payPalGateway.sendPayment(userEmail, amount);
        System.out.println("PayPal payment processed via Adapter for " + userEmail + " amounting to $" + amount);
        return true; // Assuming success for mock
    }
}
