import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.EmptyBorder; // Added for card padding
import java.util.Vector; // Added for table model data manipulation

public class Dashboard {
    private JPanel panel1; // Main panel from .form
    private JButton listViewButton;
    private JButton gridViewButton;
    private JScrollPane scrollPane; // Contains dataTable for list view, this is "listViewCard"
    private JTable dataTable;
    private JButton addButton;
    private JButton deleteButton;
    private JButton upgradeButton;
    private JButton viewLogsButton;
    private JFrame frame;

    // Fields for CardLayout components bound from .form
    private JPanel viewSwitchPanel; // The panel with CardLayout
    private JPanel gridViewContainerPanel; // The panel for grid view content, this is "gridViewCard"

    private DefaultTableModel tableModel; // Made tableModel a class field
    private String[] columnNames = {"Date", "Description", "Category", "Amount"}; // Column names for the table

    public Dashboard() {
        frame = new JFrame("Dashboard");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(800, 600)); // As per .form
        frame.setResizable(false);

        // viewSwitchPanel and gridViewContainerPanel are initialized by IntelliJ's form loader
        // scrollPane is also initialized and is part of viewSwitchPanel as "listViewCard"
        // gridViewContainerPanel is part of viewSwitchPanel as "gridViewCard"

        // Initialize the table model with sample data
        Object[][] initialData = {
            {"2024-05-01", "Groceries", "Food", 50.00},
            {"2024-05-03", "Gasoline", "Transport", 40.00},
            {"2024-05-05", "Movie Tickets", "Entertainment", 30.00},
            {"2024-05-06", "Books", "Education", 75.00},
            {"2024-05-08", "Dinner Out", "Food", 60.00}
        };
        tableModel = new DefaultTableModel(initialData, columnNames);
        
        if (dataTable != null) { // dataTable is bound from .form
            dataTable.setModel(tableModel);
        }

        // Populate Grid View
        populateGridView(); // Call method to populate grid view, now uses class tableModel

        // Action listener for List View button
        if (listViewButton != null) {
            listViewButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.out.println("List View Clicked");
                    if (viewSwitchPanel != null) {
                        CardLayout cl = (CardLayout) (viewSwitchPanel.getLayout());
                        cl.show(viewSwitchPanel, "listViewCard");
                    }
                }
            });
        }

        // Action listener for Grid View button
        if (gridViewButton != null) {
            gridViewButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.out.println("Grid View Clicked");
                    if (viewSwitchPanel != null) {
                        populateGridView(); // Repopulate grid view in case data changed
                        CardLayout cl = (CardLayout) (viewSwitchPanel.getLayout());
                        cl.show(viewSwitchPanel, "gridViewCard");
                    }
                }
            });
        }

        // Action listener for Add button
        if (addButton != null) {
            addButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Prompt for transaction details
                    String date = JOptionPane.showInputDialog(frame, "Enter Date (YYYY-MM-DD):", "Add Transaction", JOptionPane.PLAIN_MESSAGE);
                    if (date == null || date.trim().isEmpty()) return; // User cancelled or entered empty

                    String description = JOptionPane.showInputDialog(frame, "Enter Description:", "Add Transaction", JOptionPane.PLAIN_MESSAGE);
                    if (description == null || description.trim().isEmpty()) return;

                    String category = JOptionPane.showInputDialog(frame, "Enter Category:", "Add Transaction", JOptionPane.PLAIN_MESSAGE);
                    if (category == null || category.trim().isEmpty()) return;

                    String amountStr = JOptionPane.showInputDialog(frame, "Enter Amount:", "Add Transaction", JOptionPane.PLAIN_MESSAGE);
                    if (amountStr == null || amountStr.trim().isEmpty()) return;

                    try {
                        double amount = Double.parseDouble(amountStr);
                        tableModel.addRow(new Object[]{date, description, category, amount});
                        // JTable updates automatically. Grid view needs explicit refresh.
                        populateGridView(); 
                        System.out.println("LOG: Transaction Added - " + description + ", " + amount); // Placeholder log
                        JOptionPane.showMessageDialog(frame, "Transaction added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(frame, "Invalid amount. Please enter a numeric value.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
        }

        // Action listener for Delete button
        if (deleteButton != null) {
            deleteButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int selectedRow = dataTable.getSelectedRow();
                    if (selectedRow >= 0) {
                        // Convert view row index to model row index in case of sorting/filtering
                        int modelRow = dataTable.convertRowIndexToModel(selectedRow);
                        
                        String description = tableModel.getValueAt(modelRow, 1).toString();
                        Object amount = tableModel.getValueAt(modelRow, 3);

                        int confirm = JOptionPane.showConfirmDialog(frame, 
                            "Are you sure you want to delete this transaction?\n" + description + " - $" + amount, 
                            "Confirm Deletion", 
                            JOptionPane.YES_NO_OPTION, 
                            JOptionPane.WARNING_MESSAGE);
                        
                        if (confirm == JOptionPane.YES_OPTION) {
                            tableModel.removeRow(modelRow);
                            // JTable updates automatically. Grid view needs explicit refresh.
                            populateGridView();
                            System.out.println("LOG: Transaction Deleted - " + description + ", " + amount); // Placeholder log
                            JOptionPane.showMessageDialog(frame, "Transaction deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(frame, "Please select a transaction from the list to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
                    }
                }
            });
        }

        // Action listener for Upgrade button
        if (upgradeButton != null) {
            upgradeButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Open PaymentPage
                    new PaymentPage();
                    // Optionally, you might want to close or hide the dashboard
                    // frame.dispose(); // or frame.setVisible(false);
                }
            });
        }


        frame.setContentPane(panel1); // Set panel1 (from .form) as the content pane
        frame.pack();

        // Ensure list view is shown initially
        if (viewSwitchPanel != null && viewSwitchPanel.getLayout() instanceof CardLayout) {
            CardLayout cl = (CardLayout) (viewSwitchPanel.getLayout());
            cl.show(viewSwitchPanel, "listViewCard");
        } else {
            System.err.println("viewSwitchPanel is not ready for CardLayout initialization in constructor.");
        }

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void populateGridView() { // Removed parameters, now uses class field tableModel
        if (gridViewContainerPanel == null) {
            System.err.println("gridViewContainerPanel is null, cannot populate grid view.");
            return;
        }
        if (tableModel == null) {
            System.err.println("tableModel is null, cannot populate grid view.");
            return;
        }

        gridViewContainerPanel.removeAll(); 
        gridViewContainerPanel.setLayout(new BorderLayout());

        JPanel actualGridHolder = new JPanel();
        
        // Get data from the tableModel
        Vector<Vector> dataVector = tableModel.getDataVector();
        int numItems = dataVector.size();

        int numColumns = 2; 
        int numRows = (numItems == 0) ? 1 : (int) Math.ceil((double) numItems / numColumns); // Ensure at least 1 row for empty message
        
        actualGridHolder.setLayout(new GridLayout(numRows, numColumns, 10, 10)); 
        actualGridHolder.setBorder(new EmptyBorder(10, 10, 10, 10)); 
        actualGridHolder.setBackground(Color.decode("#E0E0E0")); 

        if (numItems == 0) {
            JLabel emptyLabel = new JLabel("No transactions to display.", SwingConstants.CENTER);
            emptyLabel.setFont(new Font("Roboto", Font.ITALIC, 16));
            // Add to a panel that will be centered in actualGridHolder if GridLayout has 1,1
            // For simplicity, if grid is 1x2 due to numColumns=2, this might not look perfect.
            // A better way for empty state is to have a separate panel for it.
            // For now, just add it directly.
            actualGridHolder.setLayout(new BorderLayout()); // Change layout for single empty message
            actualGridHolder.add(emptyLabel, BorderLayout.CENTER);
        } else {
            for (Vector<Object> rowVector : dataVector) {
                JPanel cardPanel = new JPanel(new BorderLayout(5, 5));
                cardPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.GRAY, 1),
                    new EmptyBorder(10, 10, 10, 10) 
                ));
                cardPanel.setBackground(Color.WHITE);

                // Data from rowVector: Date, Description, Category, Amount
                JLabel descriptionLabel = new JLabel("<html><b>Desc:</b> " + rowVector.get(1).toString() + "</html>");
                JLabel categoryLabel = new JLabel("Category: " + rowVector.get(2).toString());
                JLabel dateLabel = new JLabel("Date: " + rowVector.get(0).toString());
                JLabel amountLabel = new JLabel(String.format("<html><b>Amount: $%.2f</b></html>", (Double) rowVector.get(3)));
                
                Font defaultFont = new Font("Roboto", Font.PLAIN, 12);
                descriptionLabel.setFont(defaultFont);
                categoryLabel.setFont(defaultFont);
                dateLabel.setFont(defaultFont);
                amountLabel.setFont(new Font("Roboto", Font.BOLD, 13));

                JPanel textPanel = new JPanel(); 
                textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
                textPanel.setBackground(Color.WHITE); 
                textPanel.add(descriptionLabel);
                textPanel.add(categoryLabel);
                textPanel.add(dateLabel);
                textPanel.add(Box.createRigidArea(new Dimension(0, 5))); 
                textPanel.add(amountLabel);

                cardPanel.add(textPanel, BorderLayout.CENTER);
                
                actualGridHolder.add(cardPanel);
            }
            
            // Fill remaining cells if GridLayout is used and items don't fill the last row
            if (actualGridHolder.getLayout() instanceof GridLayout) {
                 int totalCells = numRows * numColumns;
                 for (int i = numItems; i < totalCells; i++) {
                    JPanel emptyPanel = new JPanel();
                    emptyPanel.setBackground(actualGridHolder.getBackground()); 
                    actualGridHolder.add(emptyPanel);
                }
            }
        }

        JScrollPane gridScrollPane = new JScrollPane(actualGridHolder);
        gridScrollPane.setBorder(BorderFactory.createEmptyBorder()); 
        gridScrollPane.getViewport().setBackground(actualGridHolder.getBackground());

        gridViewContainerPanel.add(gridScrollPane, BorderLayout.CENTER);
        gridViewContainerPanel.revalidate();
        gridViewContainerPanel.repaint();
    }

    // TODO: Add methods for data management, upgradeButton, viewLogsButton action listeners etc.
}
