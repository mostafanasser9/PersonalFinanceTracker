// No package declaration needed if it's in the default src directory with other classes
import java.awt.Color;

// Proxy Interface
interface TransactionFlagging {
    void flagTransaction(int modelRow, DisplayableTransaction transactionToFlag, Color color);
    void unflagTransaction(int modelRow, DisplayableTransaction transactionToFlag);
}
