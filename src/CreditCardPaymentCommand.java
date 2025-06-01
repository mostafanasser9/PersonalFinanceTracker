public class CreditCardPaymentCommand implements Command {
    private PaymentProcessor paymentProcessor;
    private double amount;
    private String userIdentifier;
    private String cardNumber;
    private String expiryDate;
    private String cvv;

    public CreditCardPaymentCommand(PaymentProcessor paymentProcessor, double amount, String userIdentifier, String cardNumber, String expiryDate, String cvv) {
        this.paymentProcessor = paymentProcessor;
        this.amount = amount;
        this.userIdentifier = userIdentifier;
        this.cardNumber = cardNumber;
        this.expiryDate = expiryDate;
        this.cvv = cvv;
    }

    @Override
    public boolean execute() {
        // Assumes paymentProcessor is a CreditCardAdapter, which uses this signature
        return paymentProcessor.processPayment(amount, userIdentifier, cardNumber, expiryDate, cvv);
    }
}
