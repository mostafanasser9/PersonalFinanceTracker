import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class TransactionManagerFacade {
    private ArrayList<DisplayableTransaction> transactions;
    private HistoryLogger historyLogger;

    public TransactionManagerFacade() {
        this.transactions = new ArrayList<>();
        this.historyLogger = HistoryLogger.getInstance();
        loadSampleTransactionsInternal(); // Load sample data on initialization
    }

    public ArrayList<DisplayableTransaction> getTransactions() {
        return new ArrayList<>(transactions); // Return a copy to prevent external modification
    }

    public DisplayableTransaction getTransaction(int index) {
        if (index >= 0 && index < transactions.size()) {
            return transactions.get(index);
        }
        return null;
    }

    public int getTransactionCount() {
        return transactions.size();
    }

    public boolean addTransaction(Transaction newTransaction) {
        if (newTransaction != null) {
            transactions.add(newTransaction);
            historyLogger.addLog("Transaction Added: " + newTransaction.getDescription() + " $" + String.format("%.2f", newTransaction.getAmount()));
            return true;
        }
        return false;
    }

    public boolean deleteTransaction(DisplayableTransaction transactionToDelete) {
        if (transactionToDelete != null && transactions.remove(transactionToDelete)) {
            historyLogger.addLog("Transaction Deleted: " + transactionToDelete.getDescription() + " $" + String.format("%.2f", transactionToDelete.getAmount()));
            return true;
        }
        return false;
    }

    public void updateTransaction(int modelRow, DisplayableTransaction transaction) {
        if (modelRow >= 0 && modelRow < transactions.size()) {
            transactions.set(modelRow, transaction);
            // Note: Specific logging for "flagged" or "unflagged" is handled by RealTransactionFlagging
            // or the Dashboard. This is a generic update.
        }
    }

    private void loadSampleTransactionsInternal() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            transactions.add(new Transaction.TransactionBuilder("Groceries", 50.00).date(sdf.parse("2024-05-01")).category("Food").type("Expense").build());
            transactions.add(new Transaction.TransactionBuilder("Salary", 2500.00).date(sdf.parse("2024-05-01")).category("Income").type("Income").build());
            transactions.add(new Transaction.TransactionBuilder("Gasoline", 40.00).date(sdf.parse("2024-05-03")).category("Transport").type("Expense").build());
            transactions.add(new Transaction.TransactionBuilder("Movie Tickets", 30.00).date(sdf.parse("2024-05-05")).category("Entertainment").type("Expense").build());
            transactions.add(new Transaction.TransactionBuilder("Freelance Work", 300.00).date(sdf.parse("2024-05-06")).category("Income").type("Income").build());
        } catch (ParseException e) {
            System.err.println("Error parsing sample dates in Facade: " + e.getMessage());
        }
    }
}
