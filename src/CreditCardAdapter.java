public class CreditCardAdapter implements PaymentProcessor {
    private CreditCardGateway creditCardGateway;

    public CreditCardAdapter(CreditCardGateway creditCardGateway) {
        this.creditCardGateway = creditCardGateway;
    }

    @Override
    public boolean processPayment(double amount, String userEmail, String... paymentDetails) {
        // paymentDetails should contain card number, expiry, cvv
        if (paymentDetails.length < 3) {
            System.err.println("Credit card details incomplete for user " + userEmail);
            return false;
        }
        String cardNumber = paymentDetails[0];
        String expiryDate = paymentDetails[1];
        String cvv = paymentDetails[2];
        boolean success = creditCardGateway.chargeCard(cardNumber, expiryDate, cvv, amount);
        if (success) {
            System.out.println("Credit Card payment processed via Adapter for " + userEmail + " with card " + cardNumber);
        }
        return success;
    }
}
