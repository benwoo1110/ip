package com.benthecat.kachow;

import java.io.IOException;
import java.nio.file.Path;

import com.benthecat.kachow.exception.KachowException;
import com.benthecat.kachow.parser.Parser;
import com.benthecat.kachow.storage.Storage;
import com.benthecat.kachow.task.Task;
import com.benthecat.kachow.task.TaskList;
import com.benthecat.kachow.ui.fx.DialogBox;
import com.benthecat.kachow.ui.Ui;
import com.benthecat.kachow.ui.fx.MainWindow;
import com.benthecat.kachow.ui.printer.FxPrinter;
import com.benthecat.kachow.ui.printer.Printer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Coordinates Kachow's UI, command parsing, task list, and persistent storage.
 */
public class Kachow {
    private static final String DATA_FILE = "./data/kachow.txt";

    private final Storage storage;
    private final TaskList tasks;
    private final Ui userInterface;
    private final Parser parser;

    public Kachow(Printer printer) {
        this(DATA_FILE, printer);
    }

    /**
     * Creates a Kachow application backed by the given task data file.
     * Invalid stored data is reported and replaced with an empty in-memory task list so the UI can still run.
     *
     * @param filePath Path to the task data file.
     */
    public Kachow(String filePath, Printer printer) {
        userInterface = new Ui(printer);
        parser = new Parser();
        storage = new Storage(Path.of(filePath));

        TaskList loadedTasks;
        try {
            loadedTasks = new TaskList(storage.load());
        } catch (KachowException exception) {
            userInterface.showLoadingError(exception);
            loadedTasks = new TaskList();
        }
        tasks = loadedTasks;
    }

    public void sendWelcomeMessage() {
        userInterface.showWelcome();
        userInterface.outputData();
    }

    public void handleUserInput(String input) {
        try {
            Parser.ParsedCommand command = parser.parse(input);
            if (!execute(command)) {
                // end the application
                return;
            }
        } catch (KachowException exception) {
            userInterface.showError(exception.getMessage());
        }
        userInterface.outputData();
    }

    /**
     * Executes one validated command.
     *
     * @param parsedCommand Command and argument produced by {@link Parser}.
     * @return {@code false} only when the application should exit.
     * @throws KachowException If a task operation cannot be completed or persisted.
     */
    private boolean execute(Parser.ParsedCommand parsedCommand) throws KachowException {
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
            tasks.add(task);
            storage.save(tasks.getTasks());
            userInterface.showTaskAdded(task, tasks.getSize());
            yield true;
        }
        case ON -> {
            var date = parser.parseDate(parsedCommand);
            userInterface.showTasksOn(date, tasks.findOn(date));
            yield true;
        }
        case MARK -> {
            Task task = tasks.mark(parser.parseTaskNumber(parsedCommand));
            storage.save(tasks.getTasks());
            userInterface.showTaskMarked(task);
            yield true;
        }
        case UNMARK -> {
            Task task = tasks.unmark(parser.parseTaskNumber(parsedCommand));
            storage.save(tasks.getTasks());
            userInterface.showTaskUnmarked(task);
            yield true;
        }
        case DELETE -> {
            Task task = tasks.delete(parser.parseTaskNumber(parsedCommand));
            storage.save(tasks.getTasks());
            userInterface.showTaskDeleted(task, tasks.getSize());
            yield true;
        }
        };
    }
}
