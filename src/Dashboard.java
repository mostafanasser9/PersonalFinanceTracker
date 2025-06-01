import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class Dashboard {
    private JPanel panel1; // Main panel from .form
    private JButton listViewButton;
    private JButton gridViewButton;
    private JScrollPane scrollPane; // Contains dataTable for list view, this is "listViewCard"
    private JTable dataTable;
    private JButton addButton;
    private JButton deleteButton;
    private JButton duplicateButton; // New button for duplicating transactions
    private JButton upgradeButton;
    private JButton viewLogsButton;
    private JFrame frame;

    // Fields for CardLayout components bound from .form
    private JPanel viewSwitchPanel; // The panel with CardLayout
    private JPanel gridViewContainerPanel; // The panel for grid view content, this is "gridViewCard"

    private DefaultTableModel tableModel;
    // Store Transaction objects directly
    private ArrayList<Transaction> transactions;
    private final String[] columnNames = {"Date", "Description", "Category", "Amount", "Type"}; // Added Type

    public Dashboard() {
        frame = new JFrame("Dashboard");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(800, 600)); // As per .form
        frame.setResizable(false);

        transactions = new ArrayList<>(); // Initialize the list of transactions

        // Initialize the table model (empty at first)
        tableModel = new DefaultTableModel(null, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Make table cells non-editable
                return false;
            }
        };
        
        if (dataTable != null) { // dataTable is bound from .form
            dataTable.setModel(tableModel);
        }

        // Load initial sample data
        loadSampleTransactions();
        refreshViews(); // Initial population of table and grid

        // Action listener for List View button
        if (listViewButton != null) {
            listViewButton.addActionListener(e -> {
                System.out.println("List View Clicked");
                if (viewSwitchPanel != null) {
                    CardLayout cl = (CardLayout) (viewSwitchPanel.getLayout());
                    cl.show(viewSwitchPanel, "listViewCard");
                }
            });
        }

        // Action listener for Grid View button
        if (gridViewButton != null) {
            gridViewButton.addActionListener(e -> {
                System.out.println("Grid View Clicked");
                if (viewSwitchPanel != null) {
                    populateGridView(); // Repopulate grid view in case data changed
                    CardLayout cl = (CardLayout) (viewSwitchPanel.getLayout());
                    cl.show(viewSwitchPanel, "gridViewCard");
                }
            });
        }

        // Action listener for Add button
        if (addButton != null) {
            addButton.addActionListener(e -> {
                // Prompt for transaction details
                JTextField dateField = new JTextField();
                JTextField descriptionField = new JTextField();
                JTextField categoryField = new JTextField();
                JTextField amountField = new JTextField();
                // Adding a combo box for Type (Income/Expense)
                JComboBox<String> typeComboBox = new JComboBox<>(new String[]{"Expense", "Income"});

                Object[] message = {
                    "Date (YYYY-MM-DD):", dateField,
                    "Description:", descriptionField,
                    "Category:", categoryField,
                    "Amount:", amountField,
                    "Type:", typeComboBox
                };

                int option = JOptionPane.showConfirmDialog(frame, message, "Add New Transaction", JOptionPane.OK_CANCEL_OPTION);
                if (option == JOptionPane.OK_OPTION) {
                    String dateStr = dateField.getText();
                    String description = descriptionField.getText();
                    String category = categoryField.getText();
                    String amountStr = amountField.getText();
                    String type = (String) typeComboBox.getSelectedItem();

                    if (dateStr.trim().isEmpty() || description.trim().isEmpty() || category.trim().isEmpty() || amountStr.trim().isEmpty()) {
                        JOptionPane.showMessageDialog(frame, "All fields must be filled.", "Input Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        Date date = sdf.parse(dateStr);
                        double amount = Double.parseDouble(amountStr);

                        // Use TransactionBuilder
                        Transaction newTransaction = new Transaction.TransactionBuilder(description, amount)
                                .date(date)
                                .category(category)
                                .type(type)
                                .build();
                        
                        transactions.add(newTransaction);
                        refreshViews(); // Refresh both table and grid view

                        System.out.println("LOG: Transaction Added - " + newTransaction.toString());
                        JOptionPane.showMessageDialog(frame, "Transaction added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    } catch (ParseException ex) {
                        JOptionPane.showMessageDialog(frame, "Invalid date format. Please use YYYY-MM-DD.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(frame, "Invalid amount. Please enter a numeric value.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    } catch (IllegalStateException ex) {
                        JOptionPane.showMessageDialog(frame, ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
        }

        // Action listener for Delete button
        if (deleteButton != null) {
            deleteButton.addActionListener(e -> {
                int selectedRow = dataTable.getSelectedRow();
                if (selectedRow >= 0) {
                    // Convert view row index to model row index in case of sorting/filtering
                    int modelRow = dataTable.convertRowIndexToModel(selectedRow);
                    
                    Transaction toDelete = transactions.get(modelRow); // Get transaction from our list

                    int confirm = JOptionPane.showConfirmDialog(frame, 
                        "Are you sure you want to delete this transaction?\\n" + 
                        toDelete.getDescription() + " - $" + String.format("%.2f", toDelete.getAmount()), 
                        "Confirm Deletion", 
                        JOptionPane.YES_NO_OPTION, 
                        JOptionPane.WARNING_MESSAGE);
                    
                    if (confirm == JOptionPane.YES_OPTION) {
                        transactions.remove(modelRow); // Remove from our list
                        refreshViews(); // Refresh both table and grid view
                        System.out.println("LOG: Transaction Deleted - " + toDelete.toString());
                        JOptionPane.showMessageDialog(frame, "Transaction deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "Please select a transaction from the list to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
                }
            });
        }

        // Action listener for Duplicate button
        if (duplicateButton != null) {
            duplicateButton.addActionListener(e -> {
                int selectedRow = dataTable.getSelectedRow();
                if (selectedRow >= 0) {
                    int modelRow = dataTable.convertRowIndexToModel(selectedRow);
                    Transaction originalTransaction = transactions.get(modelRow);

                    // Use the Prototype pattern's clone method
                    Transaction clonedTransaction = originalTransaction.clone();

                    // More interactive approach: Open dialog to edit the clone
                    JTextField dateField = new JTextField(new SimpleDateFormat("yyyy-MM-dd").format(clonedTransaction.getDate()));
                    JTextField descriptionField = new JTextField(clonedTransaction.getDescription());
                    JTextField categoryField = new JTextField(clonedTransaction.getCategory());
                    JTextField amountField = new JTextField(String.valueOf(clonedTransaction.getAmount()));
                    JComboBox<String> typeComboBox = new JComboBox<>(new String[]{"Expense", "Income"});
                    typeComboBox.setSelectedItem(clonedTransaction.getType());

                    Object[] message = {
                        "Edit Duplicated Transaction:",
                        "Date (YYYY-MM-DD):", dateField,
                        "Description:", descriptionField,
                        "Category:", categoryField,
                        "Amount:", amountField,
                        "Type:", typeComboBox
                    };

                    int option = JOptionPane.showConfirmDialog(frame, message, "Duplicate Transaction", JOptionPane.OK_CANCEL_OPTION);
                    if (option == JOptionPane.OK_OPTION) {
                        String dateStr = dateField.getText();
                        String description = descriptionField.getText();
                        String category = categoryField.getText();
                        String amountStr = amountField.getText();
                        String type = (String) typeComboBox.getSelectedItem();

                        if (dateStr.trim().isEmpty() || description.trim().isEmpty() || category.trim().isEmpty() || amountStr.trim().isEmpty()) {
                            JOptionPane.showMessageDialog(frame, "All fields must be filled.", "Input Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                            Date date = sdf.parse(dateStr);
                            double amount = Double.parseDouble(amountStr);

                            Transaction finalDuplicatedTransaction = new Transaction.TransactionBuilder(description, amount)
                                    .date(date)
                                    .category(category)
                                    .type(type)
                                    .build();
                            
                            transactions.add(finalDuplicatedTransaction);
                            refreshViews();
                            System.out.println("LOG: Transaction Duplicated and Added - " + finalDuplicatedTransaction.toString());
                            JOptionPane.showMessageDialog(frame, "Transaction duplicated and added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        } catch (ParseException ex) {
                            JOptionPane.showMessageDialog(frame, "Invalid date format. Please use YYYY-MM-DD.", "Input Error", JOptionPane.ERROR_MESSAGE);
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(frame, "Invalid amount. Please enter a numeric value.", "Input Error", JOptionPane.ERROR_MESSAGE);
                        } catch (IllegalStateException ex) {
                            JOptionPane.showMessageDialog(frame, ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "Please select a transaction from the list to duplicate.", "No Selection", JOptionPane.WARNING_MESSAGE);
                }
            });
        }

        // Action listener for Upgrade button
        if (upgradeButton != null) {
            upgradeButton.addActionListener(e -> {
                // Open PaymentPage
                new PaymentPage();
                // Optionally, you might want to close or hide the dashboard
                // frame.dispose(); // or frame.setVisible(false);
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

    private void loadSampleTransactions() {
        // Sample data using Transaction objects
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            transactions.add(new Transaction.TransactionBuilder("Groceries", 50.00).date(sdf.parse("2024-05-01")).category("Food").type("Expense").build());
            transactions.add(new Transaction.TransactionBuilder("Salary", 2500.00).date(sdf.parse("2024-05-01")).category("Income").type("Income").build());
            transactions.add(new Transaction.TransactionBuilder("Gasoline", 40.00).date(sdf.parse("2024-05-03")).category("Transport").type("Expense").build());
            transactions.add(new Transaction.TransactionBuilder("Movie Tickets", 30.00).date(sdf.parse("2024-05-05")).category("Entertainment").type("Expense").build());
            transactions.add(new Transaction.TransactionBuilder("Freelance Work", 300.00).date(sdf.parse("2024-05-06")).category("Income").type("Income").build());
        } catch (ParseException e) {
            System.err.println("Error parsing sample dates: " + e.getMessage());
        }
    }

    private void refreshTable() {
        // Clear existing rows
        tableModel.setRowCount(0);
        // Populate table from transactions list
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (Transaction t : transactions) {
            tableModel.addRow(new Object[]{
                sdf.format(t.getDate()),
                t.getDescription(),
                t.getCategory(),
                t.getAmount(),
                t.getType()
            });
        }
    }
    
    private void refreshViews() {
        refreshTable();
        populateGridView();
    }


    private void populateGridView() {
        if (gridViewContainerPanel == null) {
            System.err.println("gridViewContainerPanel is null, cannot populate grid view.");
            return;
        }
        if (transactions == null) { // Check transactions list
            System.err.println("transactions list is null, cannot populate grid view.");
            return;
        }

        gridViewContainerPanel.removeAll(); 
        gridViewContainerPanel.setLayout(new BorderLayout());

        JPanel actualGridHolder = new JPanel();
        
        int numItems = transactions.size();

        int numColumns = 2; 
        int numRows = (numItems == 0) ? 1 : (int) Math.ceil((double) numItems / numColumns);
        
        actualGridHolder.setLayout(new GridLayout(numRows, numColumns, 10, 10)); 
        actualGridHolder.setBorder(new EmptyBorder(10, 10, 10, 10)); 
        actualGridHolder.setBackground(Color.decode("#E0E0E0")); 

        if (numItems == 0) {
            JLabel emptyLabel = new JLabel("No transactions to display.", SwingConstants.CENTER);
            emptyLabel.setFont(new Font("Roboto", Font.ITALIC, 16));
            actualGridHolder.setLayout(new BorderLayout());
            actualGridHolder.add(emptyLabel, BorderLayout.CENTER);
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            for (Transaction t : transactions) {
                JPanel cardPanel = new JPanel(new BorderLayout(5, 5));
                cardPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.GRAY, 1),
                    new EmptyBorder(10, 10, 10, 10) 
                ));
                cardPanel.setBackground(Color.WHITE);

                JLabel descriptionLabel = new JLabel("<html><b>Desc:</b> " + t.getDescription() + "</html>");
                JLabel categoryLabel = new JLabel("Category: " + t.getCategory());
                JLabel dateLabel = new JLabel("Date: " + sdf.format(t.getDate()));
                JLabel amountLabel = new JLabel(String.format("<html><b>Amount: $%.2f</b> (%s)</html>", t.getAmount(), t.getType()));
                
                Font defaultFont = new Font("Roboto", Font.PLAIN, 12);
                descriptionLabel.setFont(defaultFont);
                categoryLabel.setFont(defaultFont);
                dateLabel.setFont(defaultFont);
                amountLabel.setFont(new Font("Roboto", Font.BOLD, 13));
                // Color code amount based on type
                if ("Expense".equalsIgnoreCase(t.getType())) {
                    amountLabel.setForeground(Color.RED);
                } else if ("Income".equalsIgnoreCase(t.getType())) {
                    amountLabel.setForeground(Color.GREEN.darker());
                }


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

    // TODO: Add methods for data management, viewLogsButton action listeners etc.
}
