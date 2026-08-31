package com.benthecat.kachow.storage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import com.benthecat.kachow.exception.KachowException;
import com.benthecat.kachow.parser.DateTimeParser;
import com.benthecat.kachow.task.Deadline;
import com.benthecat.kachow.task.Event;
import com.benthecat.kachow.task.Task;
import com.benthecat.kachow.task.Todo;

/**
 * Loads and saves Kachow tasks in a human-readable text file.
 */
public class Storage {
    private static final String FIELD_SEPARATOR = " | ";

    private final Path dataFile;

    /**
     * Creates storage backed by the given data file.
     *
     * @param dataFile Path to the task data file.
     */
    public Storage(Path dataFile) {
        this.dataFile = dataFile;
    }

    /**
     * Loads every task from disk. A missing file represents a new user with an empty task list.
     *
     * @return Tasks stored in the data file, in their saved order.
     * @throws KachowException If the file exists but cannot be read or contains invalid task data.
     */
    public List<Task> load() throws KachowException {
        if (!Files.exists(dataFile)) {
            return new ArrayList<>();
        }

        try {
            List<Task> tasks = new ArrayList<>();
            List<String> lines = Files.readAllLines(dataFile, StandardCharsets.UTF_8);
            for (int i = 0; i < lines.size(); i++) {
                if (!lines.get(i).isBlank()) {
                    tasks.add(parseTask(lines.get(i), i + 1));
                }
            }
            return tasks;
        } catch (IOException exception) {
            throw new KachowException("I couldn't read task data from " + dataFile + ".", exception);
        }
    }

    /**
     * Saves the complete task list, creating the data directory when it does not exist yet.
     *
     * @param tasks Tasks to persist.
     * @throws KachowException If the task data cannot be written.
     */
    public void save(List<Task> tasks) throws KachowException {
        try {
            Path parent = dataFile.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            List<String> lines = tasks.stream()
                    .map(this::formatTask)
                    .toList();
            Files.write(dataFile, lines, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new KachowException("I couldn't save task data to " + dataFile + ".", exception);
        }
    }

    /**
     * Converts one saved line into its corresponding task subtype.
     *
     * @param line Saved task record.
     * @param lineNumber One-based line number used in validation messages.
     * @return Restored task.
     * @throws KachowException If the line does not follow the storage format.
     */
    private Task parseTask(String line, int lineNumber) throws KachowException {
        String[] fields = line.split(" \\| ", -1);
        if (fields.length < 3) {
            throw createInvalidDataException(lineNumber);
        }

        boolean isDone;
        if (fields[1].equals("1")) {
            isDone = true;
        } else if (fields[1].equals("0")) {
            isDone = false;
        } else {
            throw createInvalidDataException(lineNumber);
        }

        return switch (fields[0]) {
            case "T" -> {
                requireFieldCount(fields, 3, lineNumber);
                yield new Todo(fields[2], isDone);
            }
            case "D" -> {
                requireFieldCount(fields, 4, lineNumber);
                yield parseDeadline(fields[2], fields[3], isDone, lineNumber);
            }
            case "E" -> {
                requireFieldCount(fields, 5, lineNumber);
                yield parseEvent(fields[2], fields[3], fields[4], isDone, lineNumber);
            }
            default -> throw createInvalidDataException(lineNumber);
        };
    }

    /**
     * Converts a task into one line of the storage format.
     *
     * @param task Task to convert.
     * @return Serialized task record.
     */
    private String formatTask(Task task) {
        String status = task.isDone() ? "1" : "0";
        return switch (task) {
            case Todo todo -> String.join(FIELD_SEPARATOR, "T", status, task.getDescription());
            case Deadline deadline -> String.join(FIELD_SEPARATOR, "D", status, task.getDescription(),
                DateTimeParser.formatForStorage(deadline.getByValue()));
            case Event event -> String.join(FIELD_SEPARATOR, "E", status, task.getDescription(),
                DateTimeParser.formatForStorage(event.getFrom()),
                DateTimeParser.formatForStorage(event.getTo()));
            default -> throw new IllegalArgumentException("Unsupported task type: " + task.getClass().getName());
        };
    }

    /** Restores a deadline from its canonical ISO date or date-time representation. */
    private Deadline parseDeadline(String description, String by, boolean isDone, int lineNumber)
            throws KachowException {
        return new Deadline(description, parseDateTime(by, lineNumber), isDone);
    }

    /** Restores an event while enforcing its chronological range invariant. */
    private Event parseEvent(String description, String from, String to, boolean isDone, int lineNumber)
            throws KachowException {
        try {
            return new Event(
                    description,
                    parseDateTime(from, lineNumber),
                    parseDateTime(to, lineNumber),
                    isDone);
        } catch (IllegalArgumentException exception) {
            throw createInvalidDataException(lineNumber);
        }
    }

    /**
     * Parses any stored task date/time through the common parser.
     */
    private DateTimeParser.ParsedDateTime parseDateTime(String value, int lineNumber) throws KachowException {
        try {
            return DateTimeParser.parse(value);
        } catch (DateTimeParseException exception) {
            throw createInvalidDataException(lineNumber);
        }
    }

    /**
     * Ensures a record has exactly the number of fields required by its task type.
     */
    private void requireFieldCount(String[] fields, int expectedCount, int lineNumber) throws KachowException {
        if (fields.length != expectedCount) {
            throw createInvalidDataException(lineNumber);
        }
    }

    /**
     * Creates a consistent exception for malformed saved data.
     */
    private KachowException createInvalidDataException(int lineNumber) {
        return new KachowException("Task data on line " + lineNumber + " of " + dataFile + " is invalid.");
    }
}
