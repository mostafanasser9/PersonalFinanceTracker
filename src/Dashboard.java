import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.*; // Added for custom cell rendering
import javax.swing.border.EmptyBorder; // Corrected import
import javax.swing.table.DefaultTableCellRenderer; // Corrected import
import javax.swing.table.DefaultTableModel; // Corrected import


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
    private JButton historyLogButton; // New button for History Log
    private JButton flagTransactionButton; // New button for flagging transactions
    private JFrame frame;
    private String currentUserEmail; // Added to store the logged-in user's email

    // Fields for CardLayout components bound from .form
    private JPanel viewSwitchPanel; // The panel with CardLayout
    private JPanel gridViewContainerPanel; // The panel for grid view content, this is "gridViewCard"

    private DefaultTableModel tableModel;
    // Store DisplayableTransaction objects directly
    private ArrayList<DisplayableTransaction> transactions; // Changed from Transaction to DisplayableTransaction
    private final String[] columnNames = {"Date", "Description", "Category", "Amount", "Type"}; // Added Type
    private TransactionFlagging transactionFlaggingProxy; // Added proxy

    public Dashboard(String userEmail) { // Modified constructor to accept user email
        this.currentUserEmail = userEmail;
        this.transactionFlaggingProxy = new TransactionFlaggingProxy(this.currentUserEmail, this); // Initialize proxy

        frame = new JFrame("Dashboard");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(800, 600)); // As per .form
        frame.setResizable(false);

        transactions = new ArrayList<>(); // Initialize the list of transactions

        // Initialize the table model (empty at first)
        tableModel = new DefaultTableModel(null, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        if (dataTable != null) {
            dataTable.setModel(tableModel);
            // Add custom renderer for background colors
            dataTable.setDefaultRenderer(Object.class, new TransactionTableCellRenderer());
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

                        Transaction newTransaction = new Transaction.TransactionBuilder(description, amount)
                                .date(date)
                                .category(category)
                                .type(type)
                                .build();
                        
                        transactions.add(newTransaction); // Add as DisplayableTransaction
                        refreshViews(); 
                        HistoryLogger.getInstance().addLog("Transaction Added: " + newTransaction.getDescription() + " $" + newTransaction.getAmount());
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
                    int modelRow = dataTable.convertRowIndexToModel(selectedRow);
                    // Get the DisplayableTransaction for deletion
                    DisplayableTransaction toDelete = transactions.get(modelRow);
                    handleDeleteTransaction(toDelete); // Pass DisplayableTransaction
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
                    DisplayableTransaction originalTransaction = transactions.get(modelRow);
                    handleDuplicateTransaction(originalTransaction); // Pass DisplayableTransaction
                } else {
                    JOptionPane.showMessageDialog(frame, "Please select a transaction from the list to duplicate.", "No Selection", JOptionPane.WARNING_MESSAGE);
                }
            });
        }
        
        // Action listener for Flag Transaction button
        if (flagTransactionButton != null) {
            flagTransactionButton.addActionListener(e -> {
                int selectedRow = dataTable.getSelectedRow();
                if (selectedRow >= 0) {
                    int modelRow = dataTable.convertRowIndexToModel(selectedRow);
                    DisplayableTransaction transactionToFlag = transactions.get(modelRow);
                    // Use the proxy to handle flagging
                    handleFlagTransactionViaProxy(modelRow, transactionToFlag);
                } else {
                    JOptionPane.showMessageDialog(frame, "Please select a transaction from the list to flag.", "No Selection", JOptionPane.WARNING_MESSAGE);
                }
            });
        }

        // Action listener for Upgrade button
        if (upgradeButton != null) {
            upgradeButton.addActionListener(e -> {
                // Open PaymentPage and pass the current user's email
                new PaymentPage(currentUserEmail);
                // Optionally, you might want to close or hide the dashboard
                // frame.dispose(); // or frame.setVisible(false);
            });
        }

        // Action listener for History Log button
        if (historyLogButton != null) {
            historyLogButton.addActionListener(e -> {
                // Open HistoryLogPage
                new HistoryLogPage();
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

    private void handleDeleteTransaction(DisplayableTransaction transactionToDelete) { // Parameter changed to DisplayableTransaction
        if (transactionToDelete == null) return;

        int confirm = JOptionPane.showConfirmDialog(frame,
                "Are you sure you want to delete this transaction?\\n" +
                        transactionToDelete.getDescription() + " - $" + String.format("%.2f", transactionToDelete.getAmount()),
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            transactions.remove(transactionToDelete);
            refreshViews();
            HistoryLogger.getInstance().addLog("Transaction Deleted: " + transactionToDelete.getDescription() + " $" + transactionToDelete.getAmount());
            JOptionPane.showMessageDialog(frame, "Transaction deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void handleDuplicateTransaction(DisplayableTransaction originalTransaction) { // Parameter changed
        if (originalTransaction == null) return;

        // Use cloneTransaction from DisplayableTransaction for proper cloning of decorators if any
        DisplayableTransaction clonedDisplayable = originalTransaction.cloneTransaction();

        // We need a Transaction to pre-fill fields, so if it's a decorator, get the base
        Transaction transactionToEdit;
        if (clonedDisplayable instanceof TransactionDecorator) {
            DisplayableTransaction current = clonedDisplayable;
            while (current instanceof TransactionDecorator) {
                current = ((TransactionDecorator) current).getDecoratedTransaction(); // Use getter
            }
            if (current instanceof Transaction) {
                transactionToEdit = (Transaction) current;
            } else {
                 JOptionPane.showMessageDialog(frame, "Cannot determine original transaction type for duplication.", "Error", JOptionPane.ERROR_MESSAGE);
                 return; // Or handle differently
            }
        } else if (clonedDisplayable instanceof Transaction) {
            transactionToEdit = (Transaction) clonedDisplayable;
        } else {
            JOptionPane.showMessageDialog(frame, "Unsupported transaction type for duplication.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }


        JTextField dateField = new JTextField(new SimpleDateFormat("yyyy-MM-dd").format(transactionToEdit.getDate()));
        JTextField descriptionField = new JTextField(transactionToEdit.getDescription());
        JTextField categoryField = new JTextField(transactionToEdit.getCategory());
        JTextField amountField = new JTextField(String.valueOf(transactionToEdit.getAmount()));
        JComboBox<String> typeComboBox = new JComboBox<>(new String[]{"Expense", "Income"});
        typeComboBox.setSelectedItem(transactionToEdit.getType());

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

                // Create a new base Transaction from the edited details
                Transaction finalDuplicatedTransaction = new Transaction.TransactionBuilder(description, amount)
                        .date(date)
                        .category(category)
                        .type(type)
                        .build();
                
                transactions.add(finalDuplicatedTransaction); // Add as DisplayableTransaction
                refreshViews();
                HistoryLogger.getInstance().addLog("Transaction Duplicated: " + finalDuplicatedTransaction.getDescription() + " $" + finalDuplicatedTransaction.getAmount());
                JOptionPane.showMessageDialog(frame, "Transaction duplicated and added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (ParseException ex) {
                JOptionPane.showMessageDialog(frame, "Invalid date format. Please use YYYY-MM-DD.", "Input Error", JOptionPane.ERROR_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Invalid amount. Please enter a numeric value.", "Input Error", JOptionPane.ERROR_MESSAGE);
            } catch (IllegalStateException ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Method to be called by RealTransactionFlagging
    public void updateTransactionInList(int modelRow, DisplayableTransaction transaction) {
        if (modelRow >= 0 && modelRow < transactions.size()) {
            transactions.set(modelRow, transaction);
        }
    }

    // Method to be called by RealTransactionFlagging or other internal methods
    public void refreshViews() {
        refreshTable();
        populateGridView();
    }

    private void handleFlagTransactionViaProxy(int modelRow, DisplayableTransaction transactionToFlag) {
        if (transactionToFlag == null) return;

        if (transactionToFlag instanceof FlaggedTransaction) {
            Object[] options = {"Unflag", "Change Color", "Cancel"};
            int choice = JOptionPane.showOptionDialog(frame,
                    "This transaction is already flagged. What would you like to do?",
                    "Flag Options",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null, options, options[2]);

            if (choice == JOptionPane.YES_OPTION) { // Unflag
                transactionFlaggingProxy.unflagTransaction(modelRow, transactionToFlag);
            } else if (choice == JOptionPane.NO_OPTION) { // Change Color
                // Proceed to color choice
                promptForFlagColorAndFlag(modelRow, transactionToFlag);
            }
            // If Cancel or closed, do nothing
        } else {
            // If not currently flagged, prompt for color and flag
            promptForFlagColorAndFlag(modelRow, transactionToFlag);
        }
    }

    private void promptForFlagColorAndFlag(int modelRow, DisplayableTransaction transactionToFlag) {
        String[] colors = {"Brown", "Red"};
        String chosenColorName = (String) JOptionPane.showInputDialog(frame,
                "Choose a flag color:",
                "Flag Transaction",
                JOptionPane.PLAIN_MESSAGE,
                null,
                colors,
                colors[0]);

        if (chosenColorName != null) {
            Color flagColor;
            if ("Red".equals(chosenColorName)) {
                flagColor = Color.RED;
            } else { // Brown
                flagColor = new Color(150, 75, 0); // Hex: #964B00
            }
            transactionFlaggingProxy.flagTransaction(modelRow, transactionToFlag, flagColor);
            JOptionPane.showMessageDialog(frame, "Transaction flag status updated!", "Success", JOptionPane.INFORMATION_MESSAGE);

        }
    }


    // The original handleFlagTransaction is now mostly handled by RealTransactionFlagging
    // and the proxy. We keep parts of the UI logic here or move it if it makes sense.
    // For now, the core logic is in RealTransactionFlagging, accessed via proxy.
    // The UI part (dialogs) can remain here or be refactored.
    // The method below is effectively replaced by handleFlagTransactionViaProxy and promptForFlagColorAndFlag
    private void handleFlagTransaction(int modelRow, DisplayableTransaction transactionToFlag) {
        // This method\'s logic is now split and uses the proxy.
        // See handleFlagTransactionViaProxy and promptForFlagColorAndFlag
        // If you need to call this directly, ensure it uses the proxy or refactor.
        // For now, direct calls to this might bypass the proxy if not careful.
        // It\'s better to call handleFlagTransactionViaProxy.
        System.out.println("handleFlagTransaction called - should be routed via proxy now.");
    }


    private void loadSampleTransactions() {
        // Sample data using Transaction objects (which implement DisplayableTransaction)
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
        tableModel.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (DisplayableTransaction t : transactions) { // Iterate over DisplayableTransaction
            tableModel.addRow(new Object[]{
                sdf.format(t.getDate()),
                t.getDescription(),
                t.getCategory(),
                t.getAmount(),
                t.getType()
                // Color will be handled by the cell renderer
            });
        }
    }
    
    private void populateGridView() {
        if (gridViewContainerPanel == null) {
            System.err.println("gridViewContainerPanel is null, cannot populate grid view.");
            return;
        }
        if (transactions == null) {
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
        // Keep original background for the holder, individual cards will get color
        actualGridHolder.setBackground(Color.decode("#E0E0E0")); 

        if (numItems == 0) {
            JLabel emptyLabel = new JLabel("No transactions to display.", SwingConstants.CENTER);
            emptyLabel.setFont(new Font("Roboto", Font.ITALIC, 16));
            actualGridHolder.setLayout(new BorderLayout());
            actualGridHolder.add(emptyLabel, BorderLayout.CENTER);
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            for (DisplayableTransaction t : transactions) { // Iterate over DisplayableTransaction
                final DisplayableTransaction currentTransaction = t; 
                JPanel cardPanel = new JPanel(new BorderLayout(5, 5));
                cardPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.GRAY, 1),
                    new EmptyBorder(10, 10, 10, 10) 
                ));
                // Set background color based on the transaction's display property
                cardPanel.setBackground(t.getDisplayBackgroundColor()); 

                JLabel descriptionLabel = new JLabel("<html><b>Desc:</b> " + t.getDescription() + "</html>");
                JLabel categoryLabel = new JLabel("Category: " + t.getCategory());
                JLabel dateLabel = new JLabel("Date: " + sdf.format(t.getDate()));
                JLabel amountLabel = new JLabel(String.format("<html><b>Amount: $%.2f</b> (%s)</html>", t.getAmount(), t.getType()));
                
                Font defaultFont = new Font("Roboto", Font.PLAIN, 12);
                descriptionLabel.setFont(defaultFont);
                categoryLabel.setFont(defaultFont);
                dateLabel.setFont(defaultFont);
                amountLabel.setFont(new Font("Roboto", Font.BOLD, 13));

                // Set text color based on type, but background is now from decorator
                if (Color.WHITE.equals(t.getDisplayBackgroundColor())) { // Only apply if not flagged
                    if ("Expense".equalsIgnoreCase(t.getType())) {
                        amountLabel.setForeground(Color.RED);
                    } else if ("Income".equalsIgnoreCase(t.getType())) {
                        amountLabel.setForeground(Color.GREEN.darker());
                    }
                } else { // If flagged, ensure text is readable on colored background
                    // Basic contrast: use black or white text
                    // This is a simple heuristic, more advanced contrast calculation might be needed
                    Color bgColor = t.getDisplayBackgroundColor();
                    double luminance = (0.299 * bgColor.getRed() + 0.587 * bgColor.getGreen() + 0.114 * bgColor.getBlue()) / 255;
                    if (luminance > 0.5) {
                        amountLabel.setForeground(Color.BLACK);
                        descriptionLabel.setForeground(Color.BLACK);
                        categoryLabel.setForeground(Color.BLACK);
                        dateLabel.setForeground(Color.BLACK);
                    } else {
                        amountLabel.setForeground(Color.WHITE);
                        descriptionLabel.setForeground(Color.WHITE);
                        categoryLabel.setForeground(Color.WHITE);
                        dateLabel.setForeground(Color.WHITE);
                    }
                }


                JPanel textPanel = new JPanel(); 
                textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
                textPanel.setOpaque(false); // Make text panel transparent to show cardPanel's background
                textPanel.add(descriptionLabel);
                textPanel.add(categoryLabel);
                textPanel.add(dateLabel);
                textPanel.add(Box.createRigidArea(new Dimension(0, 5))); 
                textPanel.add(amountLabel);

                cardPanel.add(textPanel, BorderLayout.CENTER);

                JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
                buttonPanel.setOpaque(false); // Make button panel transparent

                JButton cardDuplicateButton = new JButton("Duplicate");
                cardDuplicateButton.setFont(new Font("Roboto", Font.PLAIN, 10));
                cardDuplicateButton.setMargin(new Insets(2, 5, 2, 5));
                cardDuplicateButton.addActionListener(e -> handleDuplicateTransaction(currentTransaction));
                
                JButton cardDeleteButton = new JButton("Delete");
                cardDeleteButton.setFont(new Font("Roboto", Font.PLAIN, 10));
                cardDeleteButton.setMargin(new Insets(2, 5, 2, 5));
                cardDeleteButton.addActionListener(e -> handleDeleteTransaction(currentTransaction));
                
                // Add Flag button to grid items as well
                JButton cardFlagButton = new JButton("Flag");
                cardFlagButton.setFont(new Font("Roboto", Font.PLAIN, 10));
                cardFlagButton.setMargin(new Insets(2, 5, 2, 5));
                cardFlagButton.addActionListener(e -> {
                    // Find the index of this transaction to pass to handleFlagTransaction
                    int index = -1;
                    for (int i = 0; i < transactions.size(); i++) {
                        if (transactions.get(i) == currentTransaction) { // Use identity for decorated objects
                            index = i;
                            break;
                        }
                    }
                    if (index != -1) {
                        handleFlagTransaction(index, currentTransaction);
                    }
                });


                buttonPanel.add(cardDuplicateButton);
                buttonPanel.add(cardDeleteButton);
                buttonPanel.add(cardFlagButton); // Add flag button to card
                
                cardPanel.add(buttonPanel, BorderLayout.SOUTH);
                
                actualGridHolder.add(cardPanel);
            }
            // Fill empty cells in the grid
            if (actualGridHolder.getLayout() instanceof GridLayout) {
                 int totalCells = numRows * numColumns;
                 for (int i = numItems; i < totalCells; i++) {
                    JPanel emptyPanel = new JPanel();
                    emptyPanel.setBackground(actualGridHolder.getBackground()); 
                    actualGridHolder.add(emptyPanel);
                }
            }
        }
        // ... (rest of grid view setup)
        JScrollPane gridScrollPane = new JScrollPane(actualGridHolder);
        gridScrollPane.setBorder(BorderFactory.createEmptyBorder()); 
        gridScrollPane.getViewport().setBackground(actualGridHolder.getBackground());

        gridViewContainerPanel.add(gridScrollPane, BorderLayout.CENTER);
        gridViewContainerPanel.revalidate();
        gridViewContainerPanel.repaint();
    }

    // Inner class for custom cell rendering in the JTable
    class TransactionTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                     boolean isSelected, boolean hasFocus,
                                                     int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (row < transactions.size()) { // Ensure row index is valid
                DisplayableTransaction transaction = transactions.get(row);
                Color backgroundColor = transaction.getDisplayBackgroundColor();
                c.setBackground(backgroundColor);

                // Adjust text color for better contrast if background is not white
                if (!Color.WHITE.equals(backgroundColor)) {
                    double luminance = (0.299 * backgroundColor.getRed() + 0.587 * backgroundColor.getGreen() + 0.114 * backgroundColor.getBlue()) / 255;
                    if (luminance > 0.5) {
                        c.setForeground(Color.BLACK); // Light background, dark text
                    } else {
                        c.setForeground(Color.WHITE); // Dark background, light text
                    }
                } else {
                     // Reset to default foreground for non-flagged items if selected/not selected
                    if (isSelected) {
                        c.setForeground(table.getSelectionForeground());
                    } else {
                        c.setForeground(table.getForeground());
                    }
                }
            } else {
                // Default background for rows out of bounds (should not happen with correct model updates)
                c.setBackground(table.getBackground());
                c.setForeground(table.getForeground());
            }
            
            // Handle selection color
            if (isSelected) {
                if (!Color.WHITE.equals(c.getBackground())) { // If it's a custom background
                     // Blend selection with custom background or use a distinct selection color
                    c.setBackground(c.getBackground().darker()); // Example: darken custom background
                } else {
                    c.setBackground(table.getSelectionBackground()); // Default selection background
                }
            }

            return c;
        }
    }

    // TODO: Add methods for data management, viewLogsButton action listeners etc.
}
