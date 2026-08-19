package com.benthecat.kachow.task;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;
import java.util.Optional;

import com.benthecat.kachow.parser.DateTimeParser;

/**
 * Represents a task that must be completed by a specified date or time.
 */
public class Deadline extends Task {
    private final DateTimeParser.ParsedDateTime by;

    /**
     * Creates an incomplete deadline task.
     *
     * @param description text describing the task
     * @param by date by which the task must be completed
     */
    public Deadline(String description, LocalDate by) {
        this(description, new DateTimeParser.ParsedDateTime(by), false);
    }

    /**
     * Creates an incomplete deadline task with a due date and time.
     *
     * @param description text describing the task
     * @param by date and time by which the task must be completed
     */
    public Deadline(String description, LocalDateTime by) {
        this(description, new DateTimeParser.ParsedDateTime(by), false);
    }

    /**
     * Creates a deadline with a restored completion status.
     *
     * @param description text describing the task
     * @param by date by which the task must be completed
     * @param isDone whether the task has been completed
     */
    public Deadline(String description, LocalDate by, boolean isDone) {
        this(description, new DateTimeParser.ParsedDateTime(by), isDone);
    }

    /**
     * Creates a deadline with a due date, due time, and restored completion status.
     *
     * @param description text describing the task
     * @param by date and time by which the task must be completed
     * @param isDone whether the task has been completed
     */
    public Deadline(String description, LocalDateTime by, boolean isDone) {
        this(description, new DateTimeParser.ParsedDateTime(by), isDone);
    }

    /**
     * Creates a deadline from a value produced by the common date/time parser.
     *
     * @param description text describing the task
     * @param by parsed due date and optional time
     */
    public Deadline(String description, DateTimeParser.ParsedDateTime by) {
        this(description, by, false);
    }

    /**
     * Creates a deadline from a parsed value with a restored completion status.
     *
     * @param description text describing the task
     * @param by parsed due date and optional time
     * @param isDone whether the task has been completed
     */
    public Deadline(String description, DateTimeParser.ParsedDateTime by, boolean isDone) {
        super(description, isDone);
        this.by = Objects.requireNonNull(by);
    }

    /**
     * Returns the deadline's due date without display formatting.
     *
     * @return deadline date
     */
    public LocalDate getBy() {
        return by.date();
    }

    /**
     * Returns the deadline's due time when one was supplied.
     *
     * @return optional deadline time
     */
    public Optional<LocalTime> getTime() {
        return by.time();
    }

    /**
     * Returns the full value produced by the common date/time parser.
     *
     * @return parsed due date and optional time
     */
    public DateTimeParser.ParsedDateTime getByValue() {
        return by;
    }

    /** {@inheritDoc} */
    @Override
    public boolean occursOn(LocalDate date) {
        return by.date().equals(date);
    }

    /** {@inheritDoc} */
    @Override
    public String getStatusText() {
        return "[D]" + super.getStatusText() + " (by: " + DateTimeParser.format(by) + ")";
    }
}
