package com.benthecat.kachow.storage;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.benthecat.kachow.exception.KachowException;
import com.benthecat.kachow.parser.DateTimeParser;
import com.benthecat.kachow.task.Deadline;
import com.benthecat.kachow.task.Event;
import com.benthecat.kachow.task.Task;
import com.benthecat.kachow.task.Todo;

/**
 * Tests persistence behavior represented by the UI startup and storage test cases.
 */
class StorageTest {
    @TempDir
    Path tempDirectory;

    /** Verifies that a missing data file is treated as an empty task list. */
    @Test
    void load_missingFile_returnsEmptyList() throws KachowException {
        Storage storage = new Storage(tempDirectory.resolve("data/kachow.txt"));

        assertTrue(storage.load().isEmpty());
    }

    /** Verifies round-trip persistence of task types, order, values, and completion state. */
    @Test
    void saveAndLoad_allTaskTypes_preserveOrderValuesAndCompletion() throws KachowException, IOException {
        Path dataFile = tempDirectory.resolve("data/kachow.txt");
        Storage storage = new Storage(dataFile);
        Todo todo = new Todo("read book", true);
        Deadline deadline = new Deadline("return book", LocalDate.of(2019, 6, 6));
        Event event = new Event(
                "project meeting",
                new DateTimeParser.ParsedDateTime(LocalDateTime.of(2019, 8, 6, 14, 0)),
                new DateTimeParser.ParsedDateTime(LocalDateTime.of(2019, 8, 6, 16, 0)));

        storage.save(List.of(todo, deadline, event));
        List<Task> loaded = storage.load();

        assertAll(
                () -> assertEquals(List.of(
                        "T | 1 | read book",
                        "D | 0 | return book | 2019-06-06",
                        "E | 0 | project meeting | 2019-08-06T14:00 | 2019-08-06T16:00"),
                        Files.readAllLines(dataFile, StandardCharsets.UTF_8)),
                () -> assertEquals(3, loaded.size()),
                () -> assertInstanceOf(Todo.class, loaded.get(0)),
                () -> assertInstanceOf(Deadline.class, loaded.get(1)),
                () -> assertInstanceOf(Event.class, loaded.get(2)),
                () -> assertEquals("[T][X] read book", loaded.get(0).getStatusText()),
                () -> assertEquals("[D][ ] return book (by: Jun 06 2019)",
                        loaded.get(1).getStatusText()),
                () -> assertEquals(
                        "[E][ ] project meeting (from: Aug 06 2019, 2:00 PM"
                                + " to: Aug 06 2019, 4:00 PM)",
                        loaded.get(2).getStatusText()));
    }

    /** Verifies that blank records are ignored without affecting surrounding task data. */
    @Test
    void load_blankLines_ignoresThemAndKeepsPhysicalLineNumbers() throws IOException, KachowException {
        Path dataFile = tempDirectory.resolve("kachow.txt");
        Files.writeString(dataFile, "\nT | 1 | read book\n\n", StandardCharsets.UTF_8);

        List<Task> loaded = new Storage(dataFile).load();

        assertAll(
                () -> assertEquals(1, loaded.size()),
                () -> assertEquals("[T][X] read book", loaded.getFirst().getStatusText()));
    }

    /** Verifies that a stored event with a reversed range reports its physical line. */
    @Test
    void load_invalidEventRange_throwsLineSpecificError() throws IOException {
        Path dataFile = tempDirectory.resolve("kachow.txt");
        Files.writeString(dataFile,
                "E | 0 | backwards | 2024-01-02T18:00 | 2024-01-02T17:00\n",
                StandardCharsets.UTF_8);

        KachowException exception = assertThrows(KachowException.class,
                () -> new Storage(dataFile).load());

        assertEquals("Task data on line 1 of " + dataFile + " is invalid.", exception.getMessage());
    }

    /** Verifies line-specific errors for invalid completion fields and record lengths. */
    @Test
    void load_malformedStatusAndFieldCount_throwLineSpecificErrors() throws IOException {
        Path invalidStatusFile = tempDirectory.resolve("invalid-status.txt");
        Path invalidFieldsFile = tempDirectory.resolve("invalid-fields.txt");
        Files.writeString(invalidStatusFile, "T | X | read book\n", StandardCharsets.UTF_8);
        Files.writeString(invalidFieldsFile, "D | 0 | return book\n", StandardCharsets.UTF_8);

        KachowException invalidStatus = assertThrows(KachowException.class,
                () -> new Storage(invalidStatusFile).load());
        KachowException invalidFields = assertThrows(KachowException.class,
                () -> new Storage(invalidFieldsFile).load());

        assertAll(
                () -> assertEquals("Task data on line 1 of " + invalidStatusFile + " is invalid.",
                        invalidStatus.getMessage()),
                () -> assertEquals("Task data on line 1 of " + invalidFieldsFile + " is invalid.",
                        invalidFields.getMessage()));
    }
}
