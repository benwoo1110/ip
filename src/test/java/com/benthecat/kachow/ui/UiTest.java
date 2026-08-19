package com.benthecat.kachow.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.benthecat.kachow.parser.DateTimeParser;
import com.benthecat.kachow.task.Deadline;
import com.benthecat.kachow.task.Event;
import com.benthecat.kachow.task.TaskList;
import com.benthecat.kachow.task.Todo;

/**
 * Tests the complete console fragments used to list tasks and date matches.
 */
class UiTest {
    private final ByteArrayOutputStream output = new ByteArrayOutputStream();
    private PrintStream originalOutput;
    private Ui ui;

    @BeforeEach
    void redirectStandardOutput() {
        originalOutput = System.out;
        System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
        ui = new Ui();
    }

    @AfterEach
    void restoreStandardOutput() {
        System.setOut(originalOutput);
    }

    @Test
    void showTaskList_emptyList_printsEmptyGridMessage() {
        ui.showTaskList(new TaskList());

        assertEquals(lines(
                "    The starting grid is empty. Add a racer with todo, deadline, or event."),
                capturedOutput());
    }

    @Test
    void showTaskList_mixedTasks_printsNumberedStatusText() {
        TaskList tasks = new TaskList(List.of(
                new Todo("read book", true),
                new Deadline("return book", LocalDate.of(2019, 6, 6)),
                new Event(
                        "project meeting",
                        new DateTimeParser.ParsedDateTime(LocalDateTime.of(2019, 8, 6, 14, 0)),
                        new DateTimeParser.ParsedDateTime(LocalDateTime.of(2019, 8, 6, 16, 0)))));

        ui.showTaskList(tasks);

        assertEquals(lines(
                "    Rev up! Here are the tasks in today's race:",
                "    1.[T][X] read book",
                "    2.[D][ ] return book (by: Jun 06 2019)",
                "    3.[E][ ] project meeting (from: Aug 06 2019, 2:00 PM"
                        + " to: Aug 06 2019, 4:00 PM)"),
                capturedOutput());
    }

    @Test
    void showTasksOn_matches_printsOriginalTaskNumbers() {
        Deadline deadline = new Deadline("submit report", LocalDateTime.of(2019, 12, 3, 9, 0));
        Event event = new Event(
                "conference",
                new DateTimeParser.ParsedDateTime(LocalDateTime.of(2019, 12, 3, 23, 0)),
                new DateTimeParser.ParsedDateTime(LocalDateTime.of(2019, 12, 4, 1, 0)));
        List<TaskList.NumberedTask> matchingTasks = List.of(
                new TaskList.NumberedTask(3, deadline),
                new TaskList.NumberedTask(4, event));

        ui.showTasksOn(LocalDate.of(2019, 12, 3), matchingTasks);

        assertEquals(lines(
                "    Rev up! Here are the deadlines and events on Dec 03 2019:",
                "    3.[D][ ] submit report (by: Dec 03 2019, 9:00 AM)",
                "    4.[E][ ] conference (from: Dec 03 2019, 11:00 PM"
                        + " to: Dec 04 2019, 1:00 AM)"),
                capturedOutput());
    }

    @Test
    void showTasksOn_noMatches_printsDateSpecificMessage() {
        ui.showTasksOn(LocalDate.of(2019, 12, 5), List.of());

        assertEquals(lines("    No deadlines or events are scheduled for Dec 05 2019."),
                capturedOutput());
    }

    private String capturedOutput() {
        return output.toString(StandardCharsets.UTF_8);
    }

    private String lines(String... lines) {
        return String.join(System.lineSeparator(), lines) + System.lineSeparator();
    }
}
