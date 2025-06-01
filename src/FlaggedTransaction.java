import java.awt.Color;

public class FlaggedTransaction extends TransactionDecorator {
    private Color flagColor;

    public FlaggedTransaction(DisplayableTransaction decoratedTransaction, Color flagColor) {
        super(decoratedTransaction);
        this.flagColor = flagColor;
    }

    @Override
    public Color getDisplayBackgroundColor() {
        return flagColor;
    }

    public Color getFlagColor() {
        return flagColor;
    }

    @Override
    public String toString() {
        String colorName = "Custom";
        if (Color.RED.equals(flagColor)) {
            colorName = "Red";
        } else if (new Color(150, 75, 0).equals(flagColor)) { // Hex: #964B00
            colorName = "Brown";
        }
        return super.toString() + " [Flagged: " + colorName + "]";
    }

    @Override
    public DisplayableTransaction cloneTransaction() {
        DisplayableTransaction clonedDecorated = decoratedTransaction.cloneTransaction();
        // Create a new FlaggedTransaction with the cloned decorated transaction and the same flag color
        return new FlaggedTransaction(clonedDecorated, this.flagColor);
    }
}
