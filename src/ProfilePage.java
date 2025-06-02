import java.awt.*;
import javax.swing.*;

public class ProfilePage {
    private JPanel mainPanel; // This should be the root panel in ProfilePage.form
    private JTextField nameTextField; // Bound to the name text field in the form
    private JTextField emailTextField; // Bound to the email text field in the form
    private JButton backButton; // Bound to the back button in the form
    private JFrame frame;
    private String currentUserEmail;

    public ProfilePage(String currentUserEmail) {
        this.currentUserEmail = currentUserEmail;
        frame = new JFrame("Profile"); // Title from the form or set here
        // $$$setupUI$$$(); // Method IntelliJ usually generates to initialize components from .form
                       // If not present, ensure components are initialized (e.g. mainPanel)
                       // For this example, we assume mainPanel is initialized by the .form binding.

        // If mainPanel is null here, it means the .form isn't correctly linked or loaded.
        // You would typically see an error or NullPointerException if mainPanel is used before initialization.
        // Ensure ProfilePage.form has a root JPanel with fx:id or field name "mainPanel",
        // and text fields named "nameTextField" and "emailTextField", and a button "backButton".

        frame.setContentPane(mainPanel); // Set the content from the form
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setPreferredSize(new Dimension(500, 350)); // Or get from form
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        loadUserData();
        setupActionListeners();
    }

    private void loadUserData() {
        UserManager userManager = UserManager.getInstance();
        String userName = userManager.getUserName(currentUserEmail);
        if (nameTextField != null) {
            nameTextField.setText(userName);
            nameTextField.setEditable(false);
        }
        if (emailTextField != null) {
            emailTextField.setText(currentUserEmail);
            emailTextField.setEditable(false);
        }
    }

    private void setupActionListeners() {
        if (backButton != null) {
            backButton.addActionListener(e -> {
                new Dashboard(currentUserEmail); // Re-open dashboard
                frame.dispose(); // Close profile page
            });
        }
    }

    // If you have a ProfilePage.form, IntelliJ usually generates a method like this:
    // private void $$$setupUI$$$() { ... }
    // And calls it, or directly initializes the fields bound from the form.
    // For this manual setup to work with a .form file, ensure your .form components
    // have matching field names in this class (e.g., mainPanel, nameTextField, emailTextField, backButton).
}
