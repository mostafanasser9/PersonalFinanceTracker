import java.awt.Color;
import java.util.Date;

public interface DisplayableTransaction {
    Date getDate();
    String getDescription();
    String getCategory();
    double getAmount();
    String getType();
    String toString();
    DisplayableTransaction cloneTransaction(); // Method to handle cloning
    Color getDisplayBackgroundColor(); // Method to get background color for display
}
