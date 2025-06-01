import java.awt.Color;
import java.util.Date;

public abstract class TransactionDecorator implements DisplayableTransaction, Cloneable {
    protected DisplayableTransaction decoratedTransaction;

    public TransactionDecorator(DisplayableTransaction decoratedTransaction) {
        this.decoratedTransaction = decoratedTransaction;
    }

    public DisplayableTransaction getDecoratedTransaction() { // Added public getter
        return decoratedTransaction;
    }

    @Override
    public Date getDate() {
        return decoratedTransaction.getDate();
    }

    @Override
    public String getDescription() {
        return decoratedTransaction.getDescription();
    }

    @Override
    public String getCategory() {
        return decoratedTransaction.getCategory();
    }

    @Override
    public double getAmount() {
        return decoratedTransaction.getAmount();
    }

    @Override
    public String getType() {
        return decoratedTransaction.getType();
    }

    @Override
    public String toString() {
        return decoratedTransaction.toString();
    }

    @Override
    public DisplayableTransaction cloneTransaction() {
        try {
            DisplayableTransaction clonedDecoratedTransaction = decoratedTransaction.cloneTransaction();
            TransactionDecorator clonedDecorator = (TransactionDecorator) super.clone(); // Shallow clone of the decorator itself
            clonedDecorator.decoratedTransaction = clonedDecoratedTransaction; // Assign the cloned decorated part
            return clonedDecorator;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Failed to clone TransactionDecorator. Ensure subclasses also handle cloning if they have mutable state.", e);
        }
    }

    @Override
    public abstract Color getDisplayBackgroundColor(); // To be implemented by concrete decorators
}
