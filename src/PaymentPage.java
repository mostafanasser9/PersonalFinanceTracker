import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PaymentPage extends JFrame {
    private JPanel mainPanel;
    private JButton proceedButton;
    private JPasswordField passwordField1;
    private JTextField textField1;
    private JRadioButton payPalRadioButton;
    private JRadioButton cashRadioButton;

    public PaymentPage() {
        setTitle("Payment Page");
        setContentPane(mainPanel);  // This comes from the .form file
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);

        proceedButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(PaymentPage.this, "Upgraded To Premium Successfully");
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PaymentPage());
    }
}
