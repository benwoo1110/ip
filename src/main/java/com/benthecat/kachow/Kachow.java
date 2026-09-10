package com.benthecat.kachow;

import java.nio.file.Path;

import com.benthecat.kachow.exception.KachowException;
import com.benthecat.kachow.parser.Parser;
import com.benthecat.kachow.storage.Storage;
import com.benthecat.kachow.task.Task;
import com.benthecat.kachow.task.TaskList;
import com.benthecat.kachow.ui.Ui;
import com.benthecat.kachow.ui.printer.ConsolePrinter;
import com.benthecat.kachow.ui.printer.Printer;

/**
 * Coordinates Kachow's UI, command parsing, task list, and persistent storage.
 */
public class Kachow {
    private static final String DATA_FILE = "./data/kachow.txt";

    private final Storage storage;
    private TaskList tasks;
    private final Ui userInterface;
    private final Parser parser;
    private final KachowException loadingException;

    /**
     * Creates a Kachow application backed by the default task data file.
     *
     * @param printer Destination for user-facing output.
     */
    public Kachow(Printer printer) {
        this(DATA_FILE, printer);
    }

    /**
     * Creates a Kachow application backed by the given task data file.
     * Invalid stored data is reported and replaced with an empty in-memory task list so the UI can still run.
     *
     * @param filePath Path to the task data file.
     * @param printer Destination for user-facing output.
     */
    public Kachow(String filePath, Printer printer) {
        userInterface = new Ui(printer);
        parser = new Parser();
        storage = new Storage(Path.of(filePath));

        TaskList loadedTasks;
        KachowException loadingError = null;
        try {
            loadedTasks = new TaskList(storage.load());
        } catch (KachowException exception) {
            loadingError = exception;
            loadedTasks = new TaskList();
        }
        tasks = loadedTasks;
        loadingException = loadingError;
    }

    /** Sends the welcome message and any error encountered while loading stored tasks. */
    public void sendWelcomeMessage() {
        userInterface.showWelcome();
        showLoadingError();
        userInterface.outputData();
    }

    /**
     * Handles one command and sends its response to the configured printer.
     *
     * @param input Complete user command.
     * @return {@code false} only when the command asks the application to exit.
     */
    public boolean handleUserInput(String input) {
        boolean shouldContinue = processUserInput(input);
        userInterface.outputData();
        return shouldContinue;
    }

    /** Processes console commands until the user exits or standard input closes. */
    public void run() {
        userInterface.showDivider();
        userInterface.showWelcome();
        userInterface.showDivider();
        showLoadingError();
        userInterface.outputData();

        while (userInterface.hasNextCommand()) {
            userInterface.showDivider();
            boolean shouldContinue = processUserInput(userInterface.readCommand());
            if (!shouldContinue) {
                userInterface.outputData();
                return;
            }
            userInterface.showDivider();
            userInterface.outputData();
        }
    }

    /** Starts the console interface using the default task data file. */
    public static void main(String[] args) {
        new Kachow(new ConsolePrinter()).run();
    }

    /**
     * Processes one command without making assumptions about the output destination.
     *
     * @param input Complete user command.
     * @return {@code false} only when the command asks the application to exit.
     */
    private boolean processUserInput(String input) {
        try {
            Parser.ParsedCommand command = parser.parse(input);
            return execute(command);
        } catch (KachowException exception) {
            userInterface.showError(exception.getMessage());
            return true;
        }
    }

    /** Displays an error encountered while loading stored tasks, if any. */
    private void showLoadingError() {
        if (loadingException != null) {
            userInterface.showLoadingError(loadingException);
        }
    }

    /** Commits in-memory changes only after their complete replacement file has been saved. */
    private void saveChanges(TaskList updatedTasks) throws KachowException {
        storage.save(updatedTasks.getTasks());
        tasks = updatedTasks;
    }

    /**
     * Executes one validated command.
     *
     * @param parsedCommand Command and argument produced by {@link Parser}.
     * @return {@code false} only when the application should exit.
     * @throws KachowException If a task operation cannot be completed or persisted.
     */
    private boolean execute(Parser.ParsedCommand parsedCommand) throws KachowException {
        TaskList updatedTasks = new TaskList(tasks.getTasks());
        return switch (parsedCommand.command()) {
            case BYE -> {
                parser.requireNoArgument(parsedCommand);
                userInterface.showGoodbye();
                yield false;
            }
            case LIST -> {
                parser.requireNoArgument(parsedCommand);
                userInterface.showTaskList(tasks);
                yield true;
            }
            case FIND -> {
                String keyword = parser.parseSearchKeyword(parsedCommand);
                userInterface.showSearchResults(keyword, tasks.findByDescription(keyword));
                yield true;
            }
            case TODO, DEADLINE, EVENT -> {
                Task task = parser.parseTask(parsedCommand);
                updatedTasks.add(task);
                saveChanges(updatedTasks);
                userInterface.showTaskAdded(task, tasks.getSize());
                yield true;
            }
            case ON -> {
                var date = parser.parseDate(parsedCommand);
                userInterface.showTasksOn(date, tasks.findOn(date));
                yield true;
            }
            case MARK -> {
                int taskNumber = parser.parseTaskNumber(parsedCommand);
                Task task = tasks.get(taskNumber).withDoneStatus(true);
                updatedTasks.replace(taskNumber, task);
                saveChanges(updatedTasks);
                userInterface.showTaskMarked(task);
                yield true;
            }
            case UNMARK -> {
                int taskNumber = parser.parseTaskNumber(parsedCommand);
                Task task = tasks.get(taskNumber).withDoneStatus(false);
                updatedTasks.replace(taskNumber, task);
                saveChanges(updatedTasks);
                userInterface.showTaskUnmarked(task);
                yield true;
            }
            case EDIT -> {
                Parser.EditCommand editCommand = parser.parseEditCommand(parsedCommand);
                Task currentTask = tasks.get(editCommand.taskNumber());
                Task editedTask = parser.applyEdit(currentTask, editCommand);
                updatedTasks.replace(editCommand.taskNumber(), editedTask);
                saveChanges(updatedTasks);
                userInterface.showTaskEdited(editedTask);
                yield true;
            }
            case DELETE -> {
                Task task = updatedTasks.delete(parser.parseTaskNumber(parsedCommand));
                saveChanges(updatedTasks);
                userInterface.showTaskDeleted(task, tasks.getSize());
                yield true;
            }
        };
    }
}
