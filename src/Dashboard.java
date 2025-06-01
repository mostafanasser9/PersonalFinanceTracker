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
    private TransactionManagerFacade transactionManager; // ADDED: Facade for transaction management
    private final String[] columnNames = {"Date", "Description", "Category", "Amount", "Type"};
    private TransactionFlagging transactionFlaggingProxy;

    public Dashboard(String userEmail) {
        this.currentUserEmail = userEmail;
        this.transactionManager = new TransactionManagerFacade(); // INITIALIZE Facade
        this.transactionFlaggingProxy = new TransactionFlaggingProxy(this.currentUserEmail, this);

        frame = new JFrame("Dashboard");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(800, 600));
        frame.setResizable(false);

        tableModel = new DefaultTableModel(null, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        if (dataTable != null) {
            dataTable.setModel(tableModel);
            dataTable.setDefaultRenderer(Object.class, new TransactionTableCellRenderer());
        }

        refreshViews(); // Initial population of table and grid

        if (listViewButton != null) {
            listViewButton.addActionListener(e -> {
                System.out.println("List View Clicked");
                if (viewSwitchPanel != null) {
                    CardLayout cl = (CardLayout) (viewSwitchPanel.getLayout());
                    cl.show(viewSwitchPanel, "listViewCard");
                }
            });
        }

        if (gridViewButton != null) {
            gridViewButton.addActionListener(e -> {
                System.out.println("Grid View Clicked");
                if (viewSwitchPanel != null) {
                    populateGridView(); 
                    CardLayout cl = (CardLayout) (viewSwitchPanel.getLayout());
                    cl.show(viewSwitchPanel, "gridViewCard");
                }
            });
        }

        if (addButton != null) {
            addButton.addActionListener(e -> {
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
                            
                            if (transactionManager.addTransaction(newTransaction)) {
                                refreshViews(); 
                                JOptionPane.showMessageDialog(frame, "Transaction added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                            } else {
                                JOptionPane.showMessageDialog(frame, "Failed to add transaction.", "Error", JOptionPane.ERROR_MESSAGE);
                            }
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

        if (deleteButton != null) {
            deleteButton.addActionListener(e -> {
                int selectedRow = dataTable.getSelectedRow();
                if (selectedRow >= 0) {
                    int modelRow = dataTable.convertRowIndexToModel(selectedRow);
                    DisplayableTransaction toDelete = transactionManager.getTransaction(modelRow);
                    if (toDelete != null) {
                        handleDeleteTransaction(toDelete); 
                    } else {
                         JOptionPane.showMessageDialog(frame, "Could not find selected transaction.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "Please select a transaction from the list to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
                }
            });
        }

        if (duplicateButton != null) {
            duplicateButton.addActionListener(e -> {
                int selectedRow = dataTable.getSelectedRow();
                if (selectedRow >= 0) {
                    int modelRow = dataTable.convertRowIndexToModel(selectedRow);
                    DisplayableTransaction originalTransaction = transactionManager.getTransaction(modelRow);
                    if (originalTransaction != null) {
                        handleDuplicateTransaction(originalTransaction);
                    } else {
                        JOptionPane.showMessageDialog(frame, "Could not find selected transaction to duplicate.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "Please select a transaction from the list to duplicate.", "No Selection", JOptionPane.WARNING_MESSAGE);
                }
            });
        }
        
        if (flagTransactionButton != null) {
            flagTransactionButton.addActionListener(e -> {
                int selectedRow = dataTable.getSelectedRow();
                if (selectedRow >= 0) {
                    int modelRow = dataTable.convertRowIndexToModel(selectedRow);
                    DisplayableTransaction transactionToFlag = transactionManager.getTransaction(modelRow);
                    if (transactionToFlag != null) {
                        handleFlagTransactionViaProxy(modelRow, transactionToFlag);
                    } else {
                        JOptionPane.showMessageDialog(frame, "Could not find selected transaction to flag.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "Please select a transaction from the list to flag.", "No Selection", JOptionPane.WARNING_MESSAGE);
                }
            });
        }

        if (upgradeButton != null) {
            upgradeButton.addActionListener(e -> {
                new PaymentPage(currentUserEmail);
            });
        }

        if (historyLogButton != null) {
            historyLogButton.addActionListener(e -> {
                new HistoryLogPage();
            });
        }

        frame.setContentPane(panel1);
        frame.pack();

        if (viewSwitchPanel != null && viewSwitchPanel.getLayout() instanceof CardLayout) {
            CardLayout cl = (CardLayout) (viewSwitchPanel.getLayout());
            cl.show(viewSwitchPanel, "listViewCard");
        } else {
            System.err.println("viewSwitchPanel is not ready for CardLayout initialization in constructor.");
        }

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void handleDeleteTransaction(DisplayableTransaction transactionToDelete) {
        if (transactionToDelete == null) return;

        int confirm = JOptionPane.showConfirmDialog(frame,
                "Are you sure you want to delete this transaction?\n" +
                        transactionToDelete.getDescription() + " - $" + String.format("%.2f", transactionToDelete.getAmount()),
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (transactionManager.deleteTransaction(transactionToDelete)) {
                refreshViews();
                JOptionPane.showMessageDialog(frame, "Transaction deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(frame, "Failed to delete transaction.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleDuplicateTransaction(DisplayableTransaction originalTransaction) {
        if (originalTransaction == null) return;

        DisplayableTransaction clonedDisplayable = originalTransaction.cloneTransaction();
        Transaction transactionToEdit;

        if (clonedDisplayable instanceof TransactionDecorator) {
            DisplayableTransaction current = clonedDisplayable;
            while (current instanceof TransactionDecorator) {
                current = ((TransactionDecorator) current).getDecoratedTransaction();
            }
            if (current instanceof Transaction) {
                transactionToEdit = (Transaction) current;
            } else {
                 JOptionPane.showMessageDialog(frame, "Cannot determine original transaction type for duplication.", "Error", JOptionPane.ERROR_MESSAGE);
                 return;
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

                Transaction finalDuplicatedTransaction = new Transaction.TransactionBuilder(description, amount)
                        .date(date)
                        .category(category)
                        .type(type)
                        .build();
                
                if (transactionManager.addTransaction(finalDuplicatedTransaction)) {
                    refreshViews();
                    HistoryLogger.getInstance().addLog("Transaction Duplicated: " + finalDuplicatedTransaction.getDescription() + " $" + finalDuplicatedTransaction.getAmount());
                    JOptionPane.showMessageDialog(frame, "Transaction duplicated and added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                     JOptionPane.showMessageDialog(frame, "Failed to add duplicated transaction.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (ParseException ex) {
                JOptionPane.showMessageDialog(frame, "Invalid date format. Please use YYYY-MM-DD.", "Input Error", JOptionPane.ERROR_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Invalid amount. Please enter a numeric value.", "Input Error", JOptionPane.ERROR_MESSAGE);
            } catch (IllegalStateException ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void updateTransactionInList(int modelRow, DisplayableTransaction transaction) {
        transactionManager.updateTransaction(modelRow, transaction);
        // refreshViews() is typically called by RealTransactionFlagging after this
    }

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
                promptForFlagColorAndFlag(modelRow, transactionToFlag);
            }
        } else {
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
            } else { 
                flagColor = new Color(150, 75, 0); 
            }
            transactionFlaggingProxy.flagTransaction(modelRow, transactionToFlag, flagColor);
            JOptionPane.showMessageDialog(frame, "Transaction flag status updated!", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void handleFlagTransaction(int modelRow, DisplayableTransaction transactionToFlag) {
        System.out.println("handleFlagTransaction called - should be routed via proxy now.");
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        ArrayList<DisplayableTransaction> currentTransactions = transactionManager.getTransactions();
        for (DisplayableTransaction t : currentTransactions) { 
            tableModel.addRow(new Object[]{
                sdf.format(t.getDate()),
                t.getDescription(),
                t.getCategory(),
                t.getAmount(),
                t.getType()
            });
        }
    }
    
    private void populateGridView() {
        if (gridViewContainerPanel == null) {
            System.err.println("gridViewContainerPanel is null, cannot populate grid view.");
            return;
        }
        ArrayList<DisplayableTransaction> currentTransactions = transactionManager.getTransactions();
        if (currentTransactions == null) {
            System.err.println("transactions list from facade is null, cannot populate grid view.");
            return;
        }

        gridViewContainerPanel.removeAll(); 
        gridViewContainerPanel.setLayout(new BorderLayout());

        JPanel actualGridHolder = new JPanel();
        
        int numItems = currentTransactions.size();
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
            for (DisplayableTransaction t : currentTransactions) {
                final DisplayableTransaction currentTransaction = t; 
                JPanel cardPanel = new JPanel(new BorderLayout(5, 5));
                cardPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.GRAY, 1),
                    new EmptyBorder(10, 10, 10, 10) 
                ));
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

                if (Color.WHITE.equals(t.getDisplayBackgroundColor())) {
                    if ("Expense".equalsIgnoreCase(t.getType())) {
                        amountLabel.setForeground(Color.RED);
                    } else if ("Income".equalsIgnoreCase(t.getType())) {
                        amountLabel.setForeground(Color.GREEN.darker());
                    }
                } else { 
                    Color bgColor = t.getDisplayBackgroundColor();
                    double luminance = (0.299 * bgColor.getRed() + 0.587 * bgColor.getGreen() + 0.114 * bgColor.getBlue()) / 255;
                    if (luminance > 0.5) {
                        descriptionLabel.setForeground(Color.BLACK);
                        categoryLabel.setForeground(Color.BLACK);
                        dateLabel.setForeground(Color.BLACK);
                        amountLabel.setForeground(Color.BLACK);
                    } else {
                        descriptionLabel.setForeground(Color.WHITE);
                        categoryLabel.setForeground(Color.WHITE);
                        dateLabel.setForeground(Color.WHITE);
                        amountLabel.setForeground(Color.WHITE);
                    }
                }

                JPanel textPanel = new JPanel(); 
                textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
                textPanel.setOpaque(false); 
                textPanel.add(descriptionLabel);
                textPanel.add(categoryLabel);
                textPanel.add(dateLabel);

                JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
                buttonPanel.setOpaque(false);

                JButton cardDuplicateButton = new JButton("Duplicate");
                JButton cardDeleteButton = new JButton("Delete");
                JButton cardFlagButton = new JButton("Flag");

                // Apply custom button styling from CoffeeShopGUI (simplified example)
                Font buttonFont = new Font("Roboto", Font.BOLD, 10);
                Dimension buttonSize = new Dimension(80, 25);
                Color buttonColor = Color.decode("#5E81AC"); // Example button color
                Color buttonTextColor = Color.WHITE;

                for (JButton btn : new JButton[]{cardDuplicateButton, cardDeleteButton, cardFlagButton}) {
                    btn.setFont(buttonFont);
                    btn.setPreferredSize(buttonSize);
                    btn.setBackground(buttonColor);
                    btn.setForeground(buttonTextColor);
                    btn.setFocusPainted(false);
                    btn.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                    buttonPanel.add(btn);
                }

                cardDuplicateButton.addActionListener(e -> handleDuplicateTransaction(currentTransaction));
                cardDeleteButton.addActionListener(e -> handleDeleteTransaction(currentTransaction));
                cardFlagButton.addActionListener(e -> {
                    int index = -1;
                    ArrayList<DisplayableTransaction> latestTransactions = transactionManager.getTransactions();
                    for (int i = 0; i < latestTransactions.size(); i++) {
                        if (latestTransactions.get(i) == currentTransaction) { 
                            index = i;
                            break;
                        }
                    }
                    if (index != -1) {
                        handleFlagTransactionViaProxy(index, currentTransaction);
                    } else {
                        System.err.println("Could not find transaction in grid view for flagging.");
                    }
                });

                cardPanel.add(textPanel, BorderLayout.CENTER);
                cardPanel.add(amountLabel, BorderLayout.NORTH); 
                cardPanel.add(buttonPanel, BorderLayout.SOUTH);

                actualGridHolder.add(cardPanel);
            }
        }
        gridViewContainerPanel.add(new JScrollPane(actualGridHolder), BorderLayout.CENTER);
        gridViewContainerPanel.revalidate();
        gridViewContainerPanel.repaint();
    }

    class TransactionTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                     boolean isSelected, boolean hasFocus,
                                                     int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (row < transactionManager.getTransactionCount()) {
                DisplayableTransaction transaction = transactionManager.getTransaction(row);
                if (transaction != null) { 
                    Color backgroundColor = transaction.getDisplayBackgroundColor();
                    c.setBackground(backgroundColor);

                    if (!Color.WHITE.equals(backgroundColor)) {
                        double luminance = (0.299 * backgroundColor.getRed() + 0.587 * backgroundColor.getGreen() + 0.114 * backgroundColor.getBlue()) / 255;
                        if (luminance > 0.5) {
                            c.setForeground(Color.BLACK);
                        } else {
                            c.setForeground(Color.WHITE);
                        }
                    } else {
                        if (isSelected) {
                            c.setForeground(table.getSelectionForeground());
                        } else {
                            c.setForeground(table.getForeground());
                        }
                    }
                } else {
                    c.setBackground(table.getBackground());
                    c.setForeground(table.getForeground());
                }
            } else {
                c.setBackground(table.getBackground());
                c.setForeground(table.getForeground());
            }
            return c;
        }
    }
}
