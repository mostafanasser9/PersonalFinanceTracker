import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class HistoryLogPage extends JFrame {
    private JPanel historyLogPanel;
    private JList<String> logList;
    private JButton backButton;

    public HistoryLogPage() {
        setTitle("History Log");
        setContentPane(historyLogPanel);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(500, 429));
        setLocationRelativeTo(null); // Center the window
        setVisible(true);

        // Dummy data for now, will be replaced with actual log data
        DefaultListModel<String> listModel = new DefaultListModel<>();
        listModel.addElement("User logged in - 2025-05-30 10:00:00");
        listModel.addElement("Added transaction: Groceries $50 - 2025-05-30 10:05:00");
        listModel.addElement("Viewed dashboard - 2025-05-30 10:06:00");
        listModel.addElement("User logged out - 2025-05-30 10:15:00");
        logList.setModel(listModel);

        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); // Close this window
                // Optionally, navigate back to Dashboard if needed, though usually handled by closing
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new HistoryLogPage();
        });
    }
}
