// No package declaration needed
import java.awt.Color;
import javax.swing.JOptionPane;


// Proxy Class
public class TransactionFlaggingProxy implements TransactionFlagging {
    private RealTransactionFlagging realTransactionFlagging;
    private Dashboard dashboard; // Reference to the Dashboard to call methods

    public TransactionFlaggingProxy(String userEmail, Dashboard dashboard) {
        this.dashboard = dashboard; // Store the dashboard reference
        this.realTransactionFlagging = new RealTransactionFlagging(dashboard);
    }

    @Override
    public void flagTransaction(int modelRow, DisplayableTransaction transactionToFlag, Color color) {
        realTransactionFlagging.flagTransaction(modelRow, transactionToFlag, color);
        HistoryLogger.getInstance().addLog("Flagged transaction: " + transactionToFlag.getDescription());
    }

    @Override
    public void unflagTransaction(int modelRow, DisplayableTransaction transactionToFlag) {
        realTransactionFlagging.unflagTransaction(modelRow, transactionToFlag);
        HistoryLogger.getInstance().addLog("Unflagged transaction: " + transactionToFlag.getDescription());
    }
}
