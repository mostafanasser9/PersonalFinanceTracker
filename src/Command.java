public interface Command {
    /**
     * Executes the command.
     * @return true if the command executed successfully, false otherwise.
     */
    boolean execute();
}
