import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer; // Added import
import javax.swing.table.DefaultTableModel;

public class Dashboard {
    private JPanel panel1; // Main panel from .form
    private JButton listViewButton;
    private JButton gridViewButton;
    private JScrollPane scrollPane; // Contains dataTable for list view
    private JTable dataTable;
    private JButton addButton;
    private JButton deleteButton;
    private JButton duplicateButton;
    private JButton upgradeButton;
    private JButton historyLogButton;
    private JButton flagTransactionButton;
    private JFrame frame;
    private String currentUserEmail;
    private UserContext userContext; // Added UserContext
    private JLabel accountStatusLabel; // Added for displaying account status

    private JPanel viewSwitchPanel; // The panel with CardLayout, bound from .form
    // gridViewContainerPanel should be the actual JPanel used for the grid view card.
    // It must be added to viewSwitchPanel with the name "gridViewCard" in your .form or setup code.
    private JPanel gridViewContainerPanel; // Panel for grid view items, bound from .form
    private JButton profileButton;

    // Strategy Pattern
    private DashboardViewStrategy currentViewStrategy;
    private ListViewStrategy listViewStrategy;
    private GridViewStrategy gridViewStrategy;

    private TransactionManagerFacade transactionManager;

    // Card names - ensure these match how panels are added to viewSwitchPanel in the .form
    private static final String LIST_VIEW_CARD_NAME = "listViewCard";
    private static final String GRID_VIEW_CARD_NAME = "gridViewCard";

    public Dashboard(String userEmail) {
        this.currentUserEmail = userEmail;
        this.userContext = new UserContext(currentUserEmail, this); // Initialize UserContext
        this.transactionManager = new TransactionManagerFacade();

        // Initialize JFrame
        frame = new JFrame("Financial Dashboard - " + currentUserEmail); // Initialize frame here
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // panel1 is the root panel from Dashboard.form. It should contain viewSwitchPanel.
        // viewSwitchPanel, in turn, should contain scrollPane (as listViewCard)
        // and gridViewContainerPanel (as gridViewCard).
        // This setup is typically done in the .form file or its generated $$$setupUI$$$().
        frame.setContentPane(panel1); // panel1 should be the main content pane from your .form
        
        // Initialize accountStatusLabel and add it to the frame (e.g., to panel1 or a specific part of it)
        accountStatusLabel = new JLabel("Account Type: " + userContext.getAccountTypeName());
        // Example: Add to a specific part of panel1, assuming panel1 uses BorderLayout
        // You might need to adjust this based on your panel1's layout manager
        if (panel1 != null) { // Ensure panel1 is initialized (from .form)
            // If panel1 is the main content pane and uses BorderLayout or similar:
            // panel1.add(accountStatusLabel, BorderLayout.NORTH); 
            // Or, if you have a dedicated status bar panel within panel1:
            // someStatusBarPanel.add(accountStatusLabel);
            // For simplicity, if panel1 is a simple JPanel, just add it.
            // This might need adjustment based on how panel1 is structured in Dashboard.form
        }


        frame.setPreferredSize(new Dimension(1000, 800));

        // Ensure components from .form are initialized before use.
        // If not using IntelliJ's GUI designer to bind them, they might be null here.
        // For example, gridViewContainerPanel needs to be a valid JPanel instance.
        // If it's null and bound from the form, ensure the binding is correct.
        // If it's not bound and should be created manually:
        if (gridViewContainerPanel == null && viewSwitchPanel != null) {
            // This is a fallback if not properly bound from the .form
            // It's better if gridViewContainerPanel is defined and added to viewSwitchPanel in the .form
            gridViewContainerPanel = new JPanel();
            viewSwitchPanel.add(gridViewContainerPanel, GRID_VIEW_CARD_NAME); 
        } else if (viewSwitchPanel == null) {
            // If viewSwitchPanel itself is null, we have a bigger problem with form bindings
            // For now, create a dummy one to prevent NullPointerExceptions, but this needs fixing in the .form
            viewSwitchPanel = new JPanel(new CardLayout());
            scrollPane = new JScrollPane(); // Assuming scrollPane is the list view card
            dataTable = new JTable();
            scrollPane.setViewportView(dataTable);
            viewSwitchPanel.add(scrollPane, LIST_VIEW_CARD_NAME);
            gridViewContainerPanel = new JPanel();
            viewSwitchPanel.add(gridViewContainerPanel, GRID_VIEW_CARD_NAME);
            // panel1 should then contain this viewSwitchPanel
            if(panel1 != null) panel1.add(viewSwitchPanel);
        }


        // Setup Strategies
        listViewStrategy = new ListViewStrategy(dataTable, viewSwitchPanel, LIST_VIEW_CARD_NAME);
        // Ensure gridViewContainerPanel is properly initialized before passing to GridViewStrategy
        if (gridViewContainerPanel == null) {
            // This indicates gridViewContainerPanel was not correctly bound from the .form
            // Or it's not being created before this point if done manually.
            // For now, let's create a new one to avoid NullPointerException,
            // but this should be properly handled by the .form or its setup.
            gridViewContainerPanel = new JPanel(); 
            if (viewSwitchPanel != null) { // Add it to the card layout if viewSwitchPanel exists
                viewSwitchPanel.add(gridViewContainerPanel, GRID_VIEW_CARD_NAME);
            }
        }
        gridViewStrategy = new GridViewStrategy(gridViewContainerPanel, viewSwitchPanel, GRID_VIEW_CARD_NAME);

        currentViewStrategy = listViewStrategy; // Default to list view

        setupTable(); // Setup table model and columns for list view
        setupActionListeners();

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        loadTransactions(); // Load initial data and display using default strategy
        currentViewStrategy.showView(); // Ensure the default view is shown
    }

    private void setupTable() {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Type");
        model.addColumn("Category");
        model.addColumn("Amount");
        model.addColumn("Date");
        model.addColumn("Description");
        if (dataTable == null) dataTable = new JTable(); // Defensive initialization
        dataTable.setModel(model);
        dataTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Custom renderer for flagged status and background colors
        dataTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                try {
                    int modelRow = table.convertRowIndexToModel(row); // Important for sorted tables
                    DisplayableTransaction transaction = transactionManager.getTransaction(modelRow);
                    if (transaction != null) {
                        Color bgColor = transaction.getDisplayBackgroundColor();
                        if (bgColor != null) {
                            c.setBackground(bgColor);
                            // Determine contrasting foreground color
                            double luminance = (0.299 * bgColor.getRed() + 0.587 * bgColor.getGreen() + 0.114 * bgColor.getBlue()) / 255;
                            c.setForeground(luminance > 0.5 ? Color.BLACK : Color.WHITE);
                        } else {
                            c.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                            c.setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
                        }
                    }
                } catch (IndexOutOfBoundsException e) {
                    // Can happen if table is being updated
                     c.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                     c.setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
                }
                return c;
            }
        });
    }

    private void setupActionListeners() {
        // Ensure buttons are not null before adding listeners - important if .form bindings are problematic
        if(listViewButton != null) listViewButton.addActionListener(e -> setViewStrategy(listViewStrategy));
        if(gridViewButton != null) gridViewButton.addActionListener(e -> setViewStrategy(gridViewStrategy));
        if(addButton != null) addButton.addActionListener(e -> addTransactionDialog());
        if(deleteButton != null) deleteButton.addActionListener(e -> deleteTransaction());
        if(duplicateButton != null) duplicateButton.addActionListener(e -> duplicateTransaction());
        if(flagTransactionButton != null) flagTransactionButton.addActionListener(e -> flagTransactionDialog());
        if(upgradeButton != null) upgradeButton.addActionListener(e -> {
            if (userContext != null) {
                userContext.attemptUpgrade(frame);
            } else {
                // Fallback, though userContext should be initialized
                System.err.println("UserContext not initialized in Dashboard. Opening payment page directly.");
                // Passing null for UserContext if it\'s not available, though this path should ideally not be hit.
                new PaymentPage(currentUserEmail, null).setVisible(true);
            }
        });
        if(historyLogButton != null) historyLogButton.addActionListener(e -> openHistoryLogPage());
        if(profileButton != null) profileButton.addActionListener(e -> openProfilePage());
    }

    private void openProfilePage() {
        new ProfilePage(currentUserEmail); // Pass currentUserEmail
        frame.dispose();
    }

    private void setViewStrategy(DashboardViewStrategy strategy) {
        this.currentViewStrategy = strategy;
        loadTransactions(); // Refresh data display with the new strategy
        this.currentViewStrategy.showView(); // Switch to the new view layout
    }

    public void loadTransactions() {
        ArrayList<DisplayableTransaction> transactions = transactionManager.getTransactions();
        currentViewStrategy.displayTransactions(transactions);
        // You might want to update other UI elements like a balance label here
    }

    private void addTransactionDialog() {
        JTextField categoryField = new JTextField();
        JTextField amountField = new JTextField();
        JTextField dateField = new JTextField(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        JTextField descriptionField = new JTextField();
        JComboBox<String> typeComboBox = new JComboBox<>(new String[]{"Income", "Expense"});

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(new JLabel("Type:"));
        panel.add(typeComboBox);
        panel.add(new JLabel("Category:"));
        panel.add(categoryField);
        panel.add(new JLabel("Amount:"));
        panel.add(amountField);
        panel.add(new JLabel("Date (yyyy-MM-dd):"));
        panel.add(dateField);
        panel.add(new JLabel("Description:"));
        panel.add(descriptionField);

        int result = JOptionPane.showConfirmDialog(frame, panel, "Add Transaction",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                String type = (String) typeComboBox.getSelectedItem();
                String category = categoryField.getText();
                double amount = Double.parseDouble(amountField.getText());
                Date date = new SimpleDateFormat("yyyy-MM-dd").parse(dateField.getText());
                String description = descriptionField.getText();

                if (type == null || type.isEmpty() || category.isEmpty() || description.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "All fields must be filled.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Corrected TransactionBuilder usage
                Transaction.TransactionBuilder builder = new Transaction.TransactionBuilder(description, amount)
                        .type(type)
                        .category(category)
                        .date(date);
                // The description is already set in the constructor for TransactionBuilder
                // If you intend to override or ensure it, you might need a .description() method in the builder
                // or rely on the constructor argument. Assuming constructor sets it.
                transactionManager.addTransaction(builder.build());
                loadTransactions();
                HistoryLogger.getInstance().addLog("Added transaction: " + description);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Invalid amount format.", "Input Error", JOptionPane.ERROR_MESSAGE);
            } catch (ParseException ex) {
                JOptionPane.showMessageDialog(frame, "Invalid date format. Please use yyyy-MM-dd.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private DisplayableTransaction getSelectedTransactionForModification(String action) {
        if (currentViewStrategy instanceof ListViewStrategy) {
            if (dataTable == null || dataTable.getSelectedRow() < 0) {
                 JOptionPane.showMessageDialog(frame, "Please select a transaction from the list to " + action + ".", "No Selection", JOptionPane.WARNING_MESSAGE);
                 return null;
            }
            int modelRow = dataTable.convertRowIndexToModel(dataTable.getSelectedRow());
            return transactionManager.getTransaction(modelRow);
        } else { 
            List<DisplayableTransaction> transactions = transactionManager.getTransactions();
            if (transactions.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "No transactions available to " + action + ".", "No Transactions", JOptionPane.INFORMATION_MESSAGE);
                return null;
            }
            String[] descriptions = transactions.stream().map(DisplayableTransaction::getDescription).toArray(String[]::new);
            if (descriptions.length == 0) { // Should be caught by transactions.isEmpty() but as a safeguard
                 JOptionPane.showMessageDialog(frame, "No transaction descriptions available for selection.", "No Transactions", JOptionPane.INFORMATION_MESSAGE);
                 return null;
            }
            String chosenDesc = (String) JOptionPane.showInputDialog(frame, "Select transaction to " + action + ":",
                    "Select Transaction", JOptionPane.PLAIN_MESSAGE, null, descriptions, descriptions[0]);

            if (chosenDesc != null) {
                return transactions.stream().filter(t -> t.getDescription().equals(chosenDesc)).findFirst().orElse(null);
            }
            return null;
        }
    }

    private void deleteTransaction() {
        DisplayableTransaction toDelete = getSelectedTransactionForModification("delete");
        if (toDelete != null) {
            int confirm = JOptionPane.showConfirmDialog(frame,
                    "Are you sure you want to delete transaction: \"" + toDelete.getDescription() + "\"?", // Corrected line
                    "Confirm Deletion", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                transactionManager.deleteTransaction(toDelete);
                loadTransactions();
                HistoryLogger.getInstance().addLog("Deleted transaction: " + toDelete.getDescription());
            }
        }
    }

    private void duplicateTransaction() {
        DisplayableTransaction originalTransaction = getSelectedTransactionForModification("duplicate");
        if (originalTransaction != null) {
            DisplayableTransaction cloned = originalTransaction.cloneTransaction();
            // Corrected TransactionBuilder usage
            Transaction.TransactionBuilder builder = new Transaction.TransactionBuilder(
                    cloned.getDescription() + " (Copy)", cloned.getAmount())
                    .type(cloned.getType())
                    .category(cloned.getCategory())
                    .date(cloned.getDate());
            transactionManager.addTransaction(builder.build());
            loadTransactions();
            HistoryLogger.getInstance().addLog("Duplicated transaction: " + originalTransaction.getDescription());
        }
    }

    private void flagTransactionDialog() {
        final DisplayableTransaction transactionToFlag = getSelectedTransactionForModification("flag/unflag");
        if (transactionToFlag == null) return;

        // Determine modelRow upfront.
        final int modelRow;
        ArrayList<DisplayableTransaction> allTransactions = transactionManager.getTransactions();
        int foundModelRow = -1;
        for (int i = 0; i < allTransactions.size(); i++) {
            DisplayableTransaction currentLoopTransaction = allTransactions.get(i);
            // Robust comparison, assuming .equals() or attribute check is sufficient
            // This check was part of the original logic.
            if (currentLoopTransaction.equals(transactionToFlag) ||
               (currentLoopTransaction.getDescription().equals(transactionToFlag.getDescription()) &&
                currentLoopTransaction.getAmount() == transactionToFlag.getAmount() &&
                currentLoopTransaction.getDate().equals(transactionToFlag.getDate()))) {
                foundModelRow = i;
                break;
            }
        }

        if (foundModelRow == -1) {
             JOptionPane.showMessageDialog(frame, "Could not identify the selected transaction's position.", "Error", JOptionPane.ERROR_MESSAGE);
             return;
        }
        modelRow = foundModelRow; // Assign to final variable for use in lambda

        Runnable doFlaggingLogic = () -> {
            // modelRow and transactionToFlag are from the outer scope and effectively final.
            TransactionFlaggingProxy flaggingProxy = new TransactionFlaggingProxy(currentUserEmail, this);
            boolean isCurrentlyFlagged = transactionToFlag.getDisplayBackgroundColor() != null &&
                                       transactionToFlag.getDisplayBackgroundColor().equals(Color.ORANGE);

            if (isCurrentlyFlagged) {
                flaggingProxy.unflagTransaction(modelRow, transactionToFlag);
            } else {
                flaggingProxy.flagTransaction(modelRow, transactionToFlag, Color.ORANGE);
            }
        };

        // UserContext handles whether to execute doFlaggingLogic or show premium message
        userContext.attemptFlagTransaction(frame, doFlaggingLogic);
    }

    public void updateTransactionInList(int modelRow, DisplayableTransaction transaction) {
        transactionManager.updateTransaction(modelRow, transaction);
        loadTransactions(); 
    }

    public void refreshViews() {
        loadTransactions();
    }

    // Method for UserContext to call to update UI
    public void updateAccountStatusLabel(String accountType) {
        if (accountStatusLabel != null) {
            accountStatusLabel.setText("Account Type: " + accountType);
        }
        // Potentially refresh other UI elements that depend on account status
        // For example, enable/disable buttons, change colors, etc.
        // This might also be a good place to call loadTransactions() if the view
        // needs to change based on account type (e.g. premium features in the list/grid)
        // loadTransactions(); // Uncomment if views need to be re-rendered based on account type
    }

    private void openPaymentPage() {
        // Ensure UserContext is passed to PaymentPage
        new PaymentPage(currentUserEmail, this.userContext).setVisible(true); 
    }

    private void openHistoryLogPage() {
        new HistoryLogPage().setVisible(true); 
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Corrected registerUser call to include a name, email, and password
            UserManager.getInstance().registerUser("Test User", "test@example.com", "password");
            new Dashboard("test@example.com");
        });
    }
}
