package com.benthecat.kachow;

import java.nio.file.Path;

import com.benthecat.kachow.exception.KachowException;
import com.benthecat.kachow.parser.Parser;
import com.benthecat.kachow.storage.Storage;
import com.benthecat.kachow.task.Task;
import com.benthecat.kachow.task.TaskList;
import com.benthecat.kachow.ui.Ui;

/**
 * Coordinates Kachow's UI, command parsing, task list, and persistent storage.
 */
public class Kachow {
    private static final String DATA_FILE = "./data/kachow.txt";

    private final Storage storage;
    private final TaskList tasks;
    private final Ui ui;
    private final Parser parser;

    /**
     * Creates a Kachow application backed by the given task data file.
     * Invalid stored data is reported and replaced with an empty in-memory task list so the UI can still run.
     *
     * @param filePath path to the task data file
     */
    public Kachow(String filePath) {
        ui = new Ui();
        parser = new Parser();
        storage = new Storage(Path.of(filePath));

        ui.showWelcome();
        TaskList loadedTasks;
        try {
            loadedTasks = new TaskList(storage.load());
        } catch (KachowException exception) {
            ui.showLoadingError(exception);
            loadedTasks = new TaskList();
        }
        tasks = loadedTasks;
    }

    /** Processes commands until the user enters {@code bye} or standard input closes. */
    public void run() {
        while (ui.hasNextCommand()) {
            ui.showDivider();
            try {
                Parser.ParsedCommand command = parser.parse(ui.readCommand());
                if (!execute(command)) {
                    return;
                }
            } catch (KachowException exception) {
                ui.showError(exception.getMessage());
            }
            ui.showDivider();
        }
    }

    /**
     * Executes one validated command.
     *
     * @param parsedCommand command and argument produced by {@link Parser}
     * @return {@code false} only when the application should exit
     * @throws KachowException if a task operation cannot be completed or persisted
     */
    private boolean execute(Parser.ParsedCommand parsedCommand) throws KachowException {
        return switch (parsedCommand.command()) {
        case BYE -> {
            parser.requireNoArgument(parsedCommand);
            ui.showGoodbye();
            yield false;
        }
        case LIST -> {
            parser.requireNoArgument(parsedCommand);
            ui.showTaskList(tasks);
            yield true;
        }
        case FIND -> {
            String keyword = parser.parseSearchKeyword(parsedCommand);
            ui.showSearchResults(keyword, tasks.findByDescription(keyword));
            yield true;
        }
        case TODO, DEADLINE, EVENT -> {
            Task task = parser.parseTask(parsedCommand);
            tasks.add(task);
            storage.save(tasks.asList());
            ui.showTaskAdded(task, tasks.size());
            yield true;
        }
        case ON -> {
            var date = parser.parseDate(parsedCommand);
            ui.showTasksOn(date, tasks.findOn(date));
            yield true;
        }
        case MARK -> {
            Task task = tasks.mark(parser.parseTaskNumber(parsedCommand));
            storage.save(tasks.asList());
            ui.showTaskMarked(task);
            yield true;
        }
        case UNMARK -> {
            Task task = tasks.unmark(parser.parseTaskNumber(parsedCommand));
            storage.save(tasks.asList());
            ui.showTaskUnmarked(task);
            yield true;
        }
        case DELETE -> {
            Task task = tasks.delete(parser.parseTaskNumber(parsedCommand));
            storage.save(tasks.asList());
            ui.showTaskDeleted(task, tasks.size());
            yield true;
        }
        };
    }

    /** Starts Kachow using its default data file. */
    public static void main(String[] args) {
        new Kachow(DATA_FILE).run();
    }
}
