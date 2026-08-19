import java.time.LocalDate;

/**
 * Represents a task without an attached date or time.
 */
public class Todo extends Task {
    /**
     * Creates an incomplete todo task.
     *
     * @param description text describing the task
     */
    public Todo(String description) {
        super(description);
    }

    /**
     * Creates a todo task with a restored completion status.
     *
     * @param description text describing the task
     * @param isDone whether the task has been completed
     */
    public Todo(String description, boolean isDone) {
        super(description, isDone);
    }

    @Override
    public boolean occursOn(LocalDate date) {
        return false;
    }

    @Override
    public String getStatusText() {
        return "[T]" + super.getStatusText();
    }
}
