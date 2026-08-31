package com.benthecat.kachow.task;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.benthecat.kachow.exception.KachowException;

/**
 * Owns the application's in-memory task collection and all operations on that collection.
 */
public class TaskList {
    private final List<Task> tasks;

    /** Creates an empty task list. */
    public TaskList() {
        this(new ArrayList<>());
    }

    /**
     * Creates a task list containing the supplied tasks in their existing order.
     *
     * @param tasks Tasks to place in the list.
     */
    public TaskList(List<Task> tasks) {
        assert tasks != null : "Initial task collection must not be null";
        assert tasks.stream().allMatch(task -> task != null)
                : "Initial task collection must not contain null tasks";

        this.tasks = new ArrayList<>(tasks);
    }

    /** Adds a task to the end of the list. */
    public void add(Task task) {
        assert task != null : "Added task must not be null";

        tasks.add(task);
    }

    /** Marks a numbered task as complete and returns it. */
    public Task mark(int taskNumber) throws KachowException {
        Task task = get(taskNumber);
        task.markAsDone();
        assert task.isDone() : "Marked task must be complete";
        return task;
    }

    /** Marks a numbered task as incomplete and returns it. */
    public Task unmark(int taskNumber) throws KachowException {
        Task task = get(taskNumber);
        task.markAsNotDone();
        assert !task.isDone() : "Unmarked task must be incomplete";
        return task;
    }

    /** Removes a numbered task and returns it. */
    public Task delete(int taskNumber) throws KachowException {
        Task task = get(taskNumber);
        int previousSize = tasks.size();
        tasks.remove(taskNumber - 1);
        assert tasks.size() == previousSize - 1 : "Deleting a task must reduce the list size by one";
        return task;
    }

    /**
     * Finds deadlines and events occurring on a date while retaining their original task numbers.
     */
    public List<NumberedTask> findOn(LocalDate date) {
        List<NumberedTask> matchingTasks = new ArrayList<>();
        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            if (task.occursOn(date)) {
                matchingTasks.add(new NumberedTask(i + 1, task));
            }
        }
        return matchingTasks;
    }

    /**
     * Finds tasks whose descriptions contain a keyword, ignoring letter case.
     * The results retain their original task numbers.
     *
     * @param keyword Keyword to search for in task descriptions.
     * @return Matching tasks with their numbers from the complete list.
     */
    public List<NumberedTask> findByDescription(String keyword) {
        String normalizedKeyword = keyword.toLowerCase(Locale.ROOT);
        List<NumberedTask> matchingTasks = new ArrayList<>();
        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            String normalizedDescription = task.getDescription().toLowerCase(Locale.ROOT);
            if (normalizedDescription.contains(normalizedKeyword)) {
                matchingTasks.add(new NumberedTask(i + 1, task));
            }
        }
        return matchingTasks;
    }

    /** Returns a read-only snapshot for display or persistence. */
    public List<Task> getTasks() {
        return List.copyOf(tasks);
    }

    /** Returns the number of tasks currently in the list. */
    public int getSize() {
        return tasks.size();
    }

    /** Reports whether the task list has no tasks. */
    public boolean isEmpty() {
        return tasks.isEmpty();
    }

    /** Gets a task after translating its user-facing number into a list index. */
    private Task get(int taskNumber) throws KachowException {
        assert taskNumber > 0 : "Task numbers must be positive";

        if (taskNumber > tasks.size()) {
            throw new KachowException(
                    "Racer " + taskNumber + " isn't on the grid. Use list to check the task numbers.");
        }
        return tasks.get(taskNumber - 1);
    }

    /** Associates a task with the one-based number it has in the complete list. */
    public record NumberedTask(int number, Task task) { }
}
