package com.benthecat.kachow.task;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.benthecat.kachow.exception.KachowException;

/**
 * Tests task state changes, deletion, renumbering, and date-based lookup.
 */
class TaskListTest {
    @Test
    void markAndUnmark_validTaskNumber_updateOnlySelectedTask() throws KachowException {
        Todo first = new Todo("read book");
        Deadline second = new Deadline("return book", LocalDate.of(2019, 12, 2));
        TaskList tasks = new TaskList(List.of(first, second));

        Task marked = tasks.mark(2);

        assertAll(
                () -> assertSame(second, marked),
                () -> assertFalse(first.isDone()),
                () -> assertTrue(second.isDone()));

        Task unmarked = tasks.unmark(2);

        assertAll(
                () -> assertSame(second, unmarked),
                () -> assertFalse(first.isDone()),
                () -> assertFalse(second.isDone()));
    }

    @Test
    void delete_firstAndLastTasks_renumbersWithoutChangingRemainingState() throws KachowException {
        Todo first = new Todo("pole position");
        Deadline second = new Deadline("refuel", LocalDate.of(2026, 8, 20), true);
        Todo third = new Todo("victory lap");
        TaskList tasks = new TaskList(List.of(first, second, third));

        assertSame(first, tasks.delete(1));
        assertSame(third, tasks.delete(2));

        assertAll(
                () -> assertEquals(1, tasks.size()),
                () -> assertSame(second, tasks.asList().getFirst()),
                () -> assertTrue(tasks.asList().getFirst().isDone()));
    }

    @Test
    void delete_outOfRangeTask_throwsWithoutChangingList() {
        Todo task = new Todo("tire change", true);
        TaskList tasks = new TaskList(List.of(task));

        KachowException exception = assertThrows(KachowException.class, () -> tasks.delete(2));

        assertAll(
                () -> assertEquals("Racer 2 isn't on the grid. Use list to check the task numbers.",
                        exception.getMessage()),
                () -> assertEquals(List.of(task), tasks.asList()),
                () -> assertTrue(task.isDone()));
    }

    @Test
    void findOn_deadlinesAndSpanningEvents_keepOriginalTaskNumbersAndExcludeTodos() {
        Todo todo = new Todo("wash car");
        Deadline deadline = new Deadline("submit report", LocalDateTime.of(2019, 12, 3, 9, 0));
        Event event = new Event(
                "conference",
                new com.benthecat.kachow.parser.DateTimeParser.ParsedDateTime(
                        LocalDateTime.of(2019, 12, 3, 23, 0)),
                new com.benthecat.kachow.parser.DateTimeParser.ParsedDateTime(
                        LocalDateTime.of(2019, 12, 4, 1, 0)));
        TaskList tasks = new TaskList(List.of(todo, deadline, event));

        List<TaskList.NumberedTask> firstDay = tasks.findOn(LocalDate.of(2019, 12, 3));
        List<TaskList.NumberedTask> secondDay = tasks.findOn(LocalDate.of(2019, 12, 4));

        assertAll(
                () -> assertEquals(List.of(2, 3),
                        firstDay.stream().map(TaskList.NumberedTask::number).toList()),
                () -> assertEquals(List.of(deadline, event),
                        firstDay.stream().map(TaskList.NumberedTask::task).toList()),
                () -> assertEquals(List.of(3),
                        secondDay.stream().map(TaskList.NumberedTask::number).toList()),
                () -> assertTrue(tasks.findOn(LocalDate.of(2019, 12, 5)).isEmpty()));
    }

    @Test
    void asList_returnedSnapshotCannotMutateTaskCollection() {
        TaskList tasks = new TaskList();
        tasks.add(new Todo("read book"));

        List<Task> snapshot = tasks.asList();

        assertThrows(UnsupportedOperationException.class, () -> snapshot.add(new Todo("write book")));
        assertEquals(1, tasks.size());
    }
}
