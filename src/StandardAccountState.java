import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class StandardAccountState implements AccountState {
    @Override
    public String getAccountTypeName() {
        return "Standard";
    }

    @Override
    public boolean canAccessPremiumFeatures() {
        return false;
    }

    @Override
    public void handleFlagTransactionAttempt(JFrame parentFrame, Runnable flagAction) {
        JOptionPane.showMessageDialog(parentFrame,
                "Flagging transactions is a premium feature. Please upgrade your account.",
                "Premium Feature", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void handleUpgradeAttempt(JFrame parentFrame, UserContext userContext) {
        // Pass UserContext so PaymentPage can notify it to refresh state upon success
        new PaymentPage(userContext.getUserEmail(), userContext).setVisible(true);
    }
}
