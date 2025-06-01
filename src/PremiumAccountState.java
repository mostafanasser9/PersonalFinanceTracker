import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class PremiumAccountState implements AccountState {
    @Override
    public String getAccountTypeName() {
        return "Premium";
    }

    @Override
    public boolean canAccessPremiumFeatures() {
        return true;
    }

    @Override
    public void handleFlagTransactionAttempt(JFrame parentFrame, Runnable flagAction) {
        flagAction.run(); // Execute the flagging logic
    }

    @Override
    public void handleUpgradeAttempt(JFrame parentFrame, UserContext userContext) {
        JOptionPane.showMessageDialog(parentFrame,
                "You are already a premium user.",
                "Account Status", JOptionPane.INFORMATION_MESSAGE);
    }
}
