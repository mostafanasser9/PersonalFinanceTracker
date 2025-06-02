import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class ListViewStrategy implements DashboardViewStrategy {
    private JTable dataTable;
    private JPanel viewSwitchPanel; // Panel with CardLayout
    private String cardName; // Name of the card for this view

    public ListViewStrategy(JTable dataTable, JPanel viewSwitchPanel, String cardName) {
        this.dataTable = dataTable;
        this.viewSwitchPanel = viewSwitchPanel;
        this.cardName = cardName;
    }

    @Override
    public void displayTransactions(List<DisplayableTransaction> transactions) {
        DefaultTableModel model = (DefaultTableModel) dataTable.getModel();
        model.setRowCount(0); // Clear existing data
        // Ensure the model has the correct columns, excluding "Flagged"
        String[] columnIdentifiers = {"Type", "Category", "Amount", "Date", "Description"};
        model.setColumnIdentifiers(columnIdentifiers);

        for (DisplayableTransaction t : transactions) {
            model.addRow(new Object[]{
                t.getType(),
                t.getCategory(),
                t.getAmount(),
                t.getDate().toString(), // Consider formatting the date
                t.getDescription(),
                // Removed t.isFlagged() or similar logic for the "Flagged" column
            });
        }
    }

    @Override
    public void showView() {
        CardLayout cl = (CardLayout) (viewSwitchPanel.getLayout());
        cl.show(viewSwitchPanel, cardName);
    }
}
