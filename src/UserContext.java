import javax.swing.JFrame;

public class UserContext {
    private AccountState currentState;
    private String userEmail;
    private Dashboard dashboard; // Optional: if UserContext needs to call back to Dashboard for UI updates

    public UserContext(String userEmail, Dashboard dashboard) {
        this.userEmail = userEmail;
        this.dashboard = dashboard; // Store dashboard reference
        refreshState(); // Initialize state based on UserManager
    }

    public void setState(AccountState state) {
        this.currentState = state;
        System.out.println("User account state changed to: " + state.getAccountTypeName());
        if (dashboard != null) {
            dashboard.updateAccountStatusLabel(state.getAccountTypeName()); // Notify Dashboard to update UI
        }
    }

    public AccountState getCurrentState() {
        return currentState;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getAccountTypeName() {
        return currentState.getAccountTypeName();
    }

    public boolean canAccessPremiumFeatures() {
        return currentState.canAccessPremiumFeatures();
    }

    public void attemptFlagTransaction(JFrame parentFrame, Runnable flagAction) {
        currentState.handleFlagTransactionAttempt(parentFrame, flagAction);
    }

    public void attemptUpgrade(JFrame parentFrame) {
        currentState.handleUpgradeAttempt(parentFrame, this);
    }

    public void refreshState() {
        boolean isPremium = UserManager.getInstance().isPremiumUser(userEmail);
        if (isPremium) {
            if (!(currentState instanceof PremiumAccountState)) {
                setState(new PremiumAccountState());
            }
        } else {
            if (!(currentState instanceof StandardAccountState)) {
                setState(new StandardAccountState());
            }
        }
    }
}
