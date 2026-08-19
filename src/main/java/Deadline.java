/**
 * Represents a task that must be completed by a specified date or time.
 */
public class Deadline extends Task {
    private final String by;

    /**
     * Creates an incomplete deadline task.
     *
     * @param description text describing the task
     * @param by date or time by which the task must be completed
     */
    public Deadline(String description, String by) {
        super(description);
        this.by = by;
    }

    /**
     * Creates a deadline with a restored completion status.
     *
     * @param description text describing the task
     * @param by date or time by which the task must be completed
     * @param isDone whether the task has been completed
     */
    public Deadline(String description, String by, boolean isDone) {
        super(description, isDone);
        this.by = by;
    }

    /**
     * Returns the deadline's due date or time without display formatting.
     *
     * @return deadline date or time
     */
    public String getBy() {
        return by;
    }

    @Override
    public String getStatusText() {
        return "[D]" + super.getStatusText() + " (by: " + by + ")";
    }
}
