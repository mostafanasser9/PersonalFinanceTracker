import javax.swing.JFrame;

public interface AccountState {
    String getAccountTypeName();
    boolean canAccessPremiumFeatures();
    void handleFlagTransactionAttempt(JFrame parentFrame, Runnable flagAction);
    void handleUpgradeAttempt(JFrame parentFrame, UserContext userContext);
}
