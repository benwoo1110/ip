/**
 * Represents a task that takes place between specified start and end times.
 */
public class Event {
    private final String description;
    private boolean isDone;
    private final String from;
    private final String to;

    /**
     * Creates an incomplete event task.
     *
     * @param description text describing the event
     * @param from date or time at which the event starts
     * @param to date or time at which the event ends
     */
    public Event(String description, String from, String to) {
        this.description = description;
        this.isDone = false;
        this.from = from;
        this.to = to;
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
     * Returns this event's type, completion status, description, and schedule.
     *
     * @return formatted event status
     */
    public String getStatusText() {
        String statusIcon = isDone ? "[X] " : "[ ] ";
        return "[E]" + statusIcon + description + " (from: " + from + " to: " + to + ")";
    }
}
