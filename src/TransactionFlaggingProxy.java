// No package declaration needed
import java.awt.Color;
import javax.swing.JOptionPane;


// Proxy Class
public class TransactionFlaggingProxy implements TransactionFlagging {
    private RealTransactionFlagging realTransactionFlagging;
    private String userEmail; // Email of the current user
    private Dashboard dashboard; // Reference to the Dashboard to call methods

    public TransactionFlaggingProxy(String userEmail, Dashboard dashboard) {
        this.userEmail = userEmail;
        this.dashboard = dashboard; // Store the dashboard reference
        this.realTransactionFlagging = new RealTransactionFlagging(dashboard);
    }

    private boolean isUserPremium() {
        return UserManager.getInstance().isPremiumUser(userEmail);
    }

    @Override
    public void flagTransaction(int modelRow, DisplayableTransaction transactionToFlag, Color color) {
        if (isUserPremium()) {
            realTransactionFlagging.flagTransaction(modelRow, transactionToFlag, color);
        } else {
            JOptionPane.showMessageDialog(null, "This feature is available for premium users only.", "Premium Feature", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    @Override
    public void unflagTransaction(int modelRow, DisplayableTransaction transactionToFlag) {
        if (isUserPremium()) {
            realTransactionFlagging.unflagTransaction(modelRow, transactionToFlag);
        } else {
            // Unflagging might be a basic feature, or also restricted.
            // For now, let's assume unflagging is also a premium action if flagging is.
            JOptionPane.showMessageDialog(null, "This feature is available for premium users only.", "Premium Feature", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
