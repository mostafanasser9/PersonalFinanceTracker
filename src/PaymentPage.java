import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;



public class PaymentPage extends JFrame {
    private JPanel Payment;

    public JRadioButton radioButton1; // This must match the binding in the .form file
    public JRadioButton cashRadioButton; // This must match the binding in the .form file
    public JButton button1;
    private JPasswordField passwordField1;
    private JTextField textField1;

    private JFrame frame;

    public PaymentPage() {
        frame = new JFrame("Payment Page");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(600, 400));
        frame.setResizable(false);

        frame.add(Payment);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);



        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(frame, "Upgraded To Premium Successfully");
            }
        });
    }

    public static void main(String[] args) {
        new PaymentPage();
    }
}















