import java.awt.*;
import java.util.List;
import javax.swing.*;

public class GridViewStrategy implements DashboardViewStrategy {
    private JPanel gridPanel; // The panel where grid items will be added
    private JPanel viewSwitchPanel; // Panel with CardLayout
    private String cardName; // Name of the card for this view

    public GridViewStrategy(JPanel gridPanel, JPanel viewSwitchPanel, String cardName) {
        this.gridPanel = gridPanel;
        this.viewSwitchPanel = viewSwitchPanel;
        this.cardName = cardName;
        // Ensure gridPanel has a layout suitable for grid items, e.g., GridLayout or FlowLayout
        this.gridPanel.setLayout(new GridLayout(0, 3, 10, 10)); // 0 rows, 3 columns, with gaps
    }

    @Override
    public void displayTransactions(List<DisplayableTransaction> transactions) {
        gridPanel.removeAll(); // Clear previous items
        if (transactions.isEmpty()) {
            gridPanel.setLayout(new FlowLayout(FlowLayout.CENTER)); // Center if no transactions
            gridPanel.add(new JLabel("No transactions to display."));
        } else {
            // Reset to GridLayout if it was changed for empty message
            if (!(gridPanel.getLayout() instanceof GridLayout)) {
                 gridPanel.setLayout(new GridLayout(0, 3, 10, 10));
            }
            for (DisplayableTransaction t : transactions) {
                JPanel itemPanel = new JPanel();
                itemPanel.setLayout(new BoxLayout(itemPanel, BoxLayout.Y_AXIS));
                itemPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
                itemPanel.setPreferredSize(new Dimension(180, 120)); // Fixed size for grid items

                itemPanel.add(new JLabel("Type: " + t.getType()));
                itemPanel.add(new JLabel("Category: " + t.getCategory()));
                itemPanel.add(new JLabel(String.format("Amount: $%.2f", t.getAmount())));
                itemPanel.add(new JLabel("Date: " + t.getDate().toString())); // Consider formatting
                itemPanel.add(new JLabel("Desc: " + t.getDescription()));

                if (t.getDisplayBackgroundColor() != null) {
                    itemPanel.setOpaque(true);
                    itemPanel.setBackground(t.getDisplayBackgroundColor());
                }
                gridPanel.add(itemPanel);
            }
        }
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    @Override
    public void showView() {
        CardLayout cl = (CardLayout) (viewSwitchPanel.getLayout());
        cl.show(viewSwitchPanel, cardName);
    }
}
