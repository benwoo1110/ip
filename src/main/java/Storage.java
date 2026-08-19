import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads and saves Kachow tasks in a human-readable text file.
 */
public class Storage {
    private static final String FIELD_SEPARATOR = " | ";

    private final Path dataFile;

    /**
     * Creates storage backed by the given data file.
     *
     * @param dataFile path to the task data file
     */
    public Storage(Path dataFile) {
        this.dataFile = dataFile;
    }

    /**
     * Loads every task from disk. A missing file represents a new user with an empty task list.
     *
     * @return tasks stored in the data file, in their saved order
     * @throws KachowException if the file exists but cannot be read or contains invalid task data
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
     * @param tasks tasks to persist
     * @throws KachowException if the task data cannot be written
     */
    public void save(List<Task> tasks) throws KachowException {
        try {
            Path parent = dataFile.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            List<String> lines = new ArrayList<>();
            for (Task task : tasks) {
                lines.add(formatTask(task));
            }
            Files.write(dataFile, lines, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new KachowException("I couldn't save task data to " + dataFile + ".", exception);
        }
    }

    /**
     * Converts one saved line into its corresponding task subtype.
     *
     * @param line saved task record
     * @param lineNumber one-based line number used in validation messages
     * @return restored task
     * @throws KachowException if the line does not follow the storage format
     */
    private Task parseTask(String line, int lineNumber) throws KachowException {
        String[] fields = line.split(" \\| ", -1);
        if (fields.length < 3) {
            throw invalidData(lineNumber);
        }

        boolean isDone;
        if (fields[1].equals("1")) {
            isDone = true;
        } else if (fields[1].equals("0")) {
            isDone = false;
        } else {
            throw invalidData(lineNumber);
        }

        return switch (fields[0]) {
        case "T" -> {
            requireFieldCount(fields, 3, lineNumber);
            yield new Todo(fields[2], isDone);
        }
        case "D" -> {
            requireFieldCount(fields, 4, lineNumber);
            yield new Deadline(fields[2], fields[3], isDone);
        }
        case "E" -> {
            requireFieldCount(fields, 5, lineNumber);
            yield new Event(fields[2], fields[3], fields[4], isDone);
        }
        default -> throw invalidData(lineNumber);
        };
    }

    /**
     * Converts a task into one line of the storage format.
     *
     * @param task task to convert
     * @return serialized task record
     */
    private String formatTask(Task task) {
        String status = task.isDone() ? "1" : "0";
        return switch (task) {
            case Todo todo -> String.join(FIELD_SEPARATOR, "T", status, task.getDescription());
            case Deadline deadline ->
                    String.join(FIELD_SEPARATOR, "D", status, task.getDescription(), deadline.getBy());
            case Event event -> String.join(
                    FIELD_SEPARATOR, "E", status, task.getDescription(), event.getFrom(), event.getTo());
            default -> throw new IllegalArgumentException("Unsupported task type: " + task.getClass().getName());
        };
    }

    /**
     * Ensures a record has exactly the number of fields required by its task type.
     */
    private void requireFieldCount(String[] fields, int expectedCount, int lineNumber) throws KachowException {
        if (fields.length != expectedCount) {
            throw invalidData(lineNumber);
        }
    }

    /**
     * Creates a consistent exception for malformed saved data.
     */
    private KachowException invalidData(int lineNumber) {
        return new KachowException("Task data on line " + lineNumber + " of " + dataFile + " is invalid.");
    }
}
