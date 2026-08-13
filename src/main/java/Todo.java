/**
 * Represents a task without an attached date or time.
 */
public class Todo {
    private final String description;
    private boolean isDone;

    /**
     * Creates an incomplete todo task.
     *
     * @param description text describing the task
     */
    public Todo(String description) {
        this.description = description;
        this.isDone = false;
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
     * Returns this todo's type, completion status, and description.
     *
     * @return formatted todo status
     */
    public String getStatusText() {
        return "[T]" + (isDone ? "[X] " : "[ ] ") + description;
    }
}
