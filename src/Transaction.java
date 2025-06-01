import PersonalFinanceTracker.DisplayableTransaction;
import java.awt.Color; // Import Color
import java.util.Date; // Import the interface

// Prototype and Builder Pattern
public class Transaction implements DisplayableTransaction, Cloneable { // Implement DisplayableTransaction
    // Attributes of a transaction
    private final Date date;
    private final String description;
    private final String category;
    private final double amount;
    private final String type; // e.g., "Expense", "Income"

    // Private constructor to be used by the Builder
    private Transaction(TransactionBuilder builder) {
        this.date = builder.date;
        this.description = builder.description;
        this.category = builder.category;
        this.amount = builder.amount;
        this.type = builder.type;
    }

    // Getters
    @Override
    public Date getDate() {
        return date;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getCategory() {
        return category;
    }

    @Override
    public double getAmount() {
        return amount;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "date=" + date +
                ", description='" + description + '\'' +
                ", category='" + category + '\'' +
                ", amount=" + amount +
                ", type='" + type + '\'' +
                '}';
    }

    // Prototype Pattern: clone method
    @Override
    public Transaction clone() {
        try {
            Transaction cloned = (Transaction) super.clone();
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(); // Should not happen
        }
    }

    // Implementation for DisplayableTransaction
    @Override
    public DisplayableTransaction cloneTransaction() {
        return this.clone(); // Use the existing clone method
    }

    @Override
    public Color getDisplayBackgroundColor() {
        return Color.WHITE; // Default background color for non-flagged transactions
    }

    // Builder Pattern: Static inner Builder class
    public static class TransactionBuilder {
        private Date date;
        private String description;
        private String category;
        private double amount;
        private String type;

        public TransactionBuilder(String description, double amount) { // Mandatory fields
            this.description = description;
            this.amount = amount;
            this.date = new Date(); // Default to now
        }

        public TransactionBuilder date(Date date) {
            this.date = date;
            return this;
        }

        public TransactionBuilder category(String category) {
            this.category = category;
            return this;
        }

        public TransactionBuilder type(String type) {
            this.type = type;
            return this;
        }
        
        public TransactionBuilder fromTransaction(Transaction transaction) {
            this.date = transaction.date;
            this.description = transaction.description;
            this.category = transaction.category;
            this.amount = transaction.amount;
            this.type = transaction.type;
            return this;
        }

        public Transaction build() {
            if (description == null || description.trim().isEmpty()) {
                throw new IllegalStateException("Description cannot be empty.");
            }
            if (type == null) {
                this.type = amount < 0 ? "Expense" : "Income";
            }
            return new Transaction(this);
        }
    }
}
