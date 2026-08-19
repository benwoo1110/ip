package com.benthecat.kachow.task;

import java.time.LocalDate;
import java.util.Objects;

import com.benthecat.kachow.parser.DateTimeParser;

/**
 * Represents a task that takes place between specified start and end times.
 */
public class Event extends Task {
    private final DateTimeParser.ParsedDateTime from;
    private final DateTimeParser.ParsedDateTime to;

    /**
     * Creates an incomplete event task.
     *
     * @param description text describing the event
     * @param from parsed date and optional time at which the event starts
     * @param to parsed date and optional time at which the event ends
     * @throws IllegalArgumentException if the event ends before it starts
     */
    public Event(String description, DateTimeParser.ParsedDateTime from, DateTimeParser.ParsedDateTime to) {
        this(description, from, to, false);
    }

    /**
     * Creates an event with a restored completion status.
     *
     * @param description text describing the event
     * @param from parsed date and optional time at which the event starts
     * @param to parsed date and optional time at which the event ends
     * @param isDone whether the event has been completed
     * @throws IllegalArgumentException if the event ends before it starts
     */
    public Event(String description, DateTimeParser.ParsedDateTime from, DateTimeParser.ParsedDateTime to,
            boolean isDone) {
        super(description, isDone);
        this.from = Objects.requireNonNull(from);
        this.to = Objects.requireNonNull(to);
        if (to.toLocalDateTime().isBefore(from.toLocalDateTime())) {
            throw new IllegalArgumentException("An event cannot end before it starts.");
        }
    }

    /**
     * Returns the event's parsed start without display formatting.
     *
     * @return parsed event start
     */
    public DateTimeParser.ParsedDateTime getFrom() {
        return from;
    }

    /**
     * Returns the event's parsed end without display formatting.
     *
     * @return parsed event end
     */
    public DateTimeParser.ParsedDateTime getTo() {
        return to;
    }

    /** {@inheritDoc} */
    @Override
    public boolean occursOn(LocalDate date) {
        return !date.isBefore(from.date()) && !date.isAfter(to.date());
    }

    /** {@inheritDoc} */
    @Override
    public String getStatusText() {
        return "[E]" + super.getStatusText()
                + " (from: " + DateTimeParser.format(from) + " to: " + DateTimeParser.format(to) + ")";
    }
}
