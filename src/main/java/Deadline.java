/**
 * Represents a task that must be completed by a specified date or time.
 */
public class Deadline {
    private final String description;
    private boolean isDone;
    private final String by;

    /**
     * Creates an incomplete deadline task.
     *
     * @param description text describing the task
     * @param by date or time by which the task must be completed
     */
    public Deadline(String description, String by) {
        this.description = description;
        this.isDone = false;
        this.by = by;
    }

    /**
     * Marks this task as complete.
     */
    public void markAsDone() {
        isDone = true;
    }

    /**
     * Marks this task as incomplete.
     */
    public void markAsNotDone() {
        isDone = false;
    }

    /**
     * Returns this deadline's type, completion status, description, and due date or time.
     *
     * @return formatted deadline status
     */
    public String getStatusText() {
        String statusIcon = isDone ? "[X] " : "[ ] ";
        return "[D]" + statusIcon + description + " (by: " + by + ")";
    }
}
