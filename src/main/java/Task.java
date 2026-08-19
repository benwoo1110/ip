/**
 * Represents a task with a description and completion status.
 */
public abstract class Task {
    private final String description;
    private boolean isDone;

    /**
     * Creates an incomplete task with the given description.
     *
     * @param description text describing the task
     */
    public Task(String description) {
        this(description, false);
    }

    /**
     * Creates a task with the given description and completion status.
     * This constructor is used when restoring a task from storage.
     *
     * @param description text describing the task
     * @param isDone whether the task has been completed
     */
    public Task(String description, boolean isDone) {
        this.description = description;
        this.isDone = isDone;
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
     * Returns the task description without display formatting.
     *
     * @return task description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Reports whether the task has been completed.
     *
     * @return {@code true} when the task is complete
     */
    public boolean isDone() {
        return isDone;
    }

    /**
     * Returns the task description prefixed with its completion icon.
     *
     * @return formatted status and description
     */
    public String getStatusText() {
        return (isDone ? "[X] " : "[ ] ") + description;
    }
}
