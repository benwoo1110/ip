package com.benthecat.kachow.ui;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

import com.benthecat.kachow.exception.KachowException;
import com.benthecat.kachow.parser.DateTimeParser;
import com.benthecat.kachow.task.Task;
import com.benthecat.kachow.task.TaskList;

/**
 * Handles all console input and user-facing output for Kachow.
 */
public class Ui {
    private static final String UI_INDENT = "    ";
    private static final String UI_DIVIDER = "____________________________________________________________";
    private static final String UI_BANNER = " _  __          _                    \n"
            + "| |/ /__ _  ___| |__   _____      __\n"
            + "| ' // _` |/ __| '_ \\ / _ \\ \\ /\\ / /\n"
            + "| . \\ (_| | (__| | | | (_) \\ V  V / \n"
            + "|_|\\_\\__,_|\\___|_| |_|\\___/ \\_/\\_/  \n";

    private final Scanner scanner;

    /** Creates a UI that reads commands from standard input. */
    public Ui() {
        scanner = new Scanner(System.in);
    }

    /** Displays the startup banner and greeting. */
    public void showWelcome() {
        showDivider();
        System.out.print(UI_BANNER.indent(UI_INDENT.length()));
        showLine("Ka-chow! I'm Kachow, the fastest chatbot on the track.");
        showLine("What can I do for you before the next lap?");
        showDivider();
    }

    /** Reports whether another console command is available. */
    public boolean hasNextCommand() {
        return scanner.hasNextLine();
    }

    /** Reads the next complete console command. */
    public String readCommand() {
        return scanner.nextLine();
    }

    /** Displays the divider that begins or ends a regular command response. */
    public void showDivider() {
        showLine(UI_DIVIDER);
    }

    /** Displays the complete task list. */
    public void showTaskList(TaskList taskList) {
        if (taskList.isEmpty()) {
            showLine("The starting grid is empty. Add a racer with todo, deadline, or event.");
            return;
        }
        showLine("Rev up! Here are the tasks in today's race:");
        List<Task> tasks = taskList.getTasks();
        for (int i = 0; i < tasks.size(); i++) {
            showLine((i + 1) + "." + tasks.get(i).getStatusText());
        }
    }

    /** Displays deadlines and events occurring on a particular date. */
    public void showTasksOn(LocalDate date, List<TaskList.NumberedTask> matchingTasks) {
        if (matchingTasks.isEmpty()) {
            showLine("No deadlines or events are scheduled for " + DateTimeParser.format(date) + ".");
            return;
        }
        showLine("Rev up! Here are the deadlines and events on " + DateTimeParser.format(date) + ":");
        for (TaskList.NumberedTask numberedTask : matchingTasks) {
            showLine(numberedTask.number() + "." + numberedTask.task().getStatusText());
        }
    }

    /** Displays confirmation after a task is added. */
    public void showTaskAdded(Task task, int taskCount) {
        showLine("Ka-chow! A new racer joined the starting grid:");
        showLine("  " + task.getStatusText());
        String racerLabel = taskCount == 1 ? " racer" : " racers";
        showLine("Now you've got " + taskCount + racerLabel + " ready to roll.");
    }

    /** Displays confirmation after a task is marked as complete. */
    public void showTaskMarked(Task task) {
        showLine("Ka-chow! This task crossed the finish line:");
        showLine("  " + task.getStatusText());
    }

    /** Displays confirmation after a task is marked as incomplete. */
    public void showTaskUnmarked(Task task) {
        showLine("Back to the starting grid! This task is not done yet:");
        showLine("  " + task.getStatusText());
    }

    /** Displays confirmation after a task is deleted. */
    public void showTaskDeleted(Task task, int taskCount) {
        showLine("Ka-chow! This racer has left the track:");
        showLine("  " + task.getStatusText());
        String racerLabel = taskCount == 1 ? " racer" : " racers";
        showLine("Now you've got " + taskCount + racerLabel + " still in the race.");
    }

    /** Displays a validation or persistence error. */
    public void showError(String message) {
        showLine("Pit stop! " + message);
    }

    /** Displays a loading error while allowing the application to start with an empty list. */
    public void showLoadingError(KachowException exception) {
        showError(exception.getMessage());
    }

    /** Displays the farewell and its closing divider. */
    public void showGoodbye() {
        showLine("Race complete! Catch you on the next lap. Ka-chow!");
        showDivider();
    }

    /** Prints one consistently indented UI line. */
    private void showLine(String text) {
        System.out.println(UI_INDENT + text);
    }
}
