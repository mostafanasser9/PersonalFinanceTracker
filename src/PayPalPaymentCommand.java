public class PayPalPaymentCommand implements Command {
    private PaymentProcessor paymentProcessor;
    private double amount;
    private String payPalEmail;

    public PayPalPaymentCommand(PaymentProcessor paymentProcessor, double amount, String payPalEmail) {
        this.paymentProcessor = paymentProcessor;
        this.amount = amount;
        this.payPalEmail = payPalEmail;
    }

    @Override
    public boolean execute() {
        // Assumes paymentProcessor is a PayPalAdapter, which uses this signature
        return paymentProcessor.processPayment(amount, payPalEmail);
    }
}
