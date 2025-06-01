import java.util.Date;

// Prototype and Builder Pattern
public class Transaction implements Cloneable {
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
    public Date getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public double getAmount() {
        return amount;
    }

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
            // Perform a shallow copy, which is fine for immutable fields (String, double)
            // and Date (Date is mutable, so for a true deep clone, Date should also be cloned if modified after creation)
            Transaction cloned = (Transaction) super.clone();
            // If Date was mutable and could be changed post-construction, you'd do:
            // cloned.date = (Date) this.date.clone();
            return cloned;
        } catch (CloneNotSupportedException e) {
            // This should not happen since we are Cloneable
            throw new AssertionError();
        }
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
        
        // Optional: Set all fields at once if coming from a prototype
        public TransactionBuilder fromTransaction(Transaction transaction) {
            this.date = transaction.date;
            this.description = transaction.description;
            this.category = transaction.category;
            this.amount = transaction.amount;
            this.type = transaction.type;
            return this;
        }

        public Transaction build() {
            // Add any validation before creating the object if necessary
            if (description == null || description.trim().isEmpty()) {
                throw new IllegalStateException("Description cannot be empty.");
            }
            if (amount == 0 && type == null) { // Basic validation example
                 this.type = "Neutral"; // Or throw error if amount is 0 for expense/income
            } else if (type == null) {
                this.type = amount < 0 ? "Expense" : "Income"; // Default type based on amount
            }
            return new Transaction(this);
        }
    }
}
