/**
 * Represents a task that takes place between specified start and end times.
 */
public class Event extends Task {
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
        super(description);
        this.from = from;
        this.to = to;
    }

    /**
     * Creates an event with a restored completion status.
     *
     * @param description text describing the event
     * @param from date or time at which the event starts
     * @param to date or time at which the event ends
     * @param isDone whether the event has been completed
     */
    public Event(String description, String from, String to, boolean isDone) {
        super(description, isDone);
        this.from = from;
        this.to = to;
    }

    /**
     * Returns the event's start without display formatting.
     *
     * @return event start
     */
    public String getFrom() {
        return from;
    }

    /**
     * Returns the event's end without display formatting.
     *
     * @return event end
     */
    public String getTo() {
        return to;
    }

    @Override
    public String getStatusText() {
        return "[E]" + super.getStatusText() + " (from: " + from + " to: " + to + ")";
    }
}
