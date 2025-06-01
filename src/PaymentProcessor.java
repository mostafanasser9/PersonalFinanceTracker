public interface PaymentProcessor {
    boolean processPayment(double amount, String userEmail, String... paymentDetails);
}
