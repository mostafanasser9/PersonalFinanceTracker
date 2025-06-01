import java.util.List;

public interface DashboardViewStrategy {
    void displayTransactions(List<DisplayableTransaction> transactions);
    void showView(); // Method to make the strategy's view visible (e.g., switch CardLayout)
}
