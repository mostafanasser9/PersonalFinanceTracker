// No package declaration needed
import java.awt.Color;

// Real Subject
class RealTransactionFlagging implements TransactionFlagging {
    private Dashboard dashboard; // Reference to the Dashboard

    public RealTransactionFlagging(Dashboard dashboard) {
        this.dashboard = dashboard; // Store the dashboard reference
    }

    @Override
    public void flagTransaction(int modelRow, DisplayableTransaction transactionToFlag, Color flagColor) {
        DisplayableTransaction baseTransaction = transactionToFlag;
        // If it was already flagged (e.g. changing color), unwrap to the actual base transaction
        if (baseTransaction instanceof TransactionDecorator) { 
             DisplayableTransaction current = baseTransaction;
             while (current instanceof TransactionDecorator) {
                current = ((TransactionDecorator) current).getDecoratedTransaction(); 
             }
             baseTransaction = current;
        }

        FlaggedTransaction flagged = new FlaggedTransaction(baseTransaction, flagColor);
        dashboard.updateTransactionInList(modelRow, flagged); // Method in Dashboard to update the list
        dashboard.refreshViews(); // Method in Dashboard to refresh UI
        HistoryLogger.getInstance().addLog("Transaction Flagged: " + flagged.getDescription());
    }

    @Override
    public void unflagTransaction(int modelRow, DisplayableTransaction transactionToFlag) {
        DisplayableTransaction original = transactionToFlag;
        while (original instanceof TransactionDecorator) {
            original = ((TransactionDecorator) original).getDecoratedTransaction(); 
        }
        dashboard.updateTransactionInList(modelRow, original); // Method in Dashboard to update the list
        dashboard.refreshViews(); // Method in Dashboard to refresh UI
        HistoryLogger.getInstance().addLog("Transaction Unflagged: " + original.getDescription());
    }
}
