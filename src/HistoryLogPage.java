import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

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

        // Load actual log data
        DefaultListModel<String> listModel = new DefaultListModel<>();
        List<String> logs = HistoryLogger.getInstance().getLogs();
        if (logs.isEmpty()) {
            listModel.addElement("No history logs recorded yet.");
        } else {
            for (String log : logs) {
                listModel.addElement(log);
            }
        }
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
