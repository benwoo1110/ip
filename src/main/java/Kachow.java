import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Starts the Kachow chatbot application.
 */
public class Kachow {
    private static final String INDENT = "    ";
    private static final String DIVIDER = "____________________________________________________________";
    private static final String DEADLINE_USAGE = "deadline DESCRIPTION /by DATE_OR_TIME";
    private static final String EVENT_USAGE = "event DESCRIPTION /from START /to END";

    /**
     * Starts Kachow and processes commands until the user enters {@code bye}.
     *
     * @param args command-line arguments, which are not used
     */
    public static void main(String[] args) {
        String banner = " _  __          _                    \n"
                + "| |/ /__ _  ___| |__   _____      __\n"
                + "| ' // _` |/ __| '_ \\ / _ \\ \\ /\\ / /\n"
                + "| . \\ (_| | (__| | | | (_) \\ V  V / \n"
                + "|_|\\_\\__,_|\\___|_| |_|\\___/ \\_/\\_/  \n";

        System.out.println(INDENT + DIVIDER);
        System.out.print(banner.indent(INDENT.length()));
        System.out.println(INDENT + "Ka-chow! I'm Kachow, the fastest chatbot on the track.");
        System.out.println(INDENT + "What can I do for you before the next lap?");
        System.out.println(INDENT + DIVIDER);

        List<Task> tasks = new ArrayList<>();
        Scanner scanner = new Scanner(System.in);
        commandLoop:
        while (scanner.hasNextLine()) {
            String command = scanner.nextLine().strip();
            int separatorIndex = firstWhitespaceIndex(command);
            String instruction = separatorIndex == -1 ? command : command.substring(0, separatorIndex);
            String argument = separatorIndex == -1 ? "" : command.substring(separatorIndex).strip();

            System.out.println(INDENT + DIVIDER);
            try {
                switch (instruction) {
                case "bye" -> {
                    if (!argument.isEmpty()) {
                        throw new KachowException("The bye command has extra cargo. Use: bye");
                    }
                    System.out.println(INDENT + "Race complete! Catch you on the next lap. Ka-chow!");
                    System.out.println(INDENT + DIVIDER);
                    break commandLoop;
                }
                case "list" -> handleListCommand(tasks, argument);
                case "mark" -> handleMarkCommand(tasks, argument);
                case "unmark" -> handleUnmarkCommand(tasks, argument);
                case "todo" -> addTodo(tasks, argument);
                case "deadline" -> addDeadline(tasks, argument);
                case "event" -> addEvent(tasks, argument);
                case "" -> throw new KachowException(
                        "That command stalled on the starting line. Enter a command to keep racing.");
                default -> throw new KachowException(
                        "That command took a wrong turn. Try todo, deadline, event, list, mark, unmark, or bye.");
                }
            } catch (KachowException exception) {
                printError(exception.getMessage());
            }
            System.out.println(INDENT + DIVIDER);
        }
    }

    /**
     * Finds the first whitespace character so commands also accept tabs and repeated spaces.
     *
     * @param text command text to inspect
     * @return the whitespace index, or {@code -1} when none exists
     */
    private static int firstWhitespaceIndex(String text) {
        for (int i = 0; i < text.length(); i++) {
            if (Character.isWhitespace(text.charAt(i))) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Lists all tasks when no unexpected argument follows the command.
     *
     * @param tasks tasks currently stored in memory
     * @param argument text following the list command
     * @throws KachowException if an unexpected argument is present
     */
    private static void handleListCommand(List<Task> tasks, String argument) throws KachowException {
        if (!argument.isEmpty()) {
            throw new KachowException("The list command has extra cargo. Use: list");
        }
        if (tasks.isEmpty()) {
            System.out.println(INDENT + "The starting grid is empty. Add a racer with todo, deadline, or event.");
            return;
        }
        System.out.println(INDENT + "Rev up! Here are the tasks in today's race:");
        for (int i = 0; i < tasks.size(); i++) {
            System.out.println(INDENT + (i + 1) + "." + tasks.get(i).getStatusText());
        }
    }

    /**
     * Adds a todo task when its description is present.
     *
     * @param tasks tasks currently stored in memory
     * @param description text describing the todo
     * @throws KachowException if the description is empty
     */
    private static void addTodo(List<Task> tasks, String description) throws KachowException {
        if (description.isBlank()) {
            throw new KachowException("This racer needs a name. Use: todo DESCRIPTION");
        }
        addTask(tasks, new Todo(description));
    }

    /**
     * Parses and adds a deadline in the form {@code DESCRIPTION /by DATE_OR_TIME}.
     *
     * @param tasks tasks currently stored in memory
     * @param argument deadline description and due date or time
     * @throws KachowException if a required deadline component is invalid or missing
     */
    private static void addDeadline(List<Task> tasks, String argument) throws KachowException {
        int byIndex = findToken(argument, "/by", 0);
        if (argument.isEmpty() || byIndex == 0) {
            throw new KachowException(
                    "This deadline racer needs a task description. Use: " + DEADLINE_USAGE);
        }
        if (byIndex == -1) {
            throw new KachowException("That deadline is missing its /by checkpoint. Use: " + DEADLINE_USAGE);
        }
        if (findToken(argument, "/by", byIndex + 3) != -1) {
            throw new KachowException(
                    "That deadline has too many /by checkpoints. Use exactly one: " + DEADLINE_USAGE);
        }

        String description = argument.substring(0, byIndex).strip();
        String by = argument.substring(byIndex + 3).strip();
        if (description.isEmpty()) {
            throw new KachowException(
                    "This deadline racer needs a task description. Use: " + DEADLINE_USAGE);
        }
        if (by.isEmpty()) {
            throw new KachowException(
                    "That deadline needs a date or time after /by. Use: " + DEADLINE_USAGE);
        }
        addTask(tasks, new Deadline(description, by));
    }

    /**
     * Parses and adds an event in the form
     * {@code DESCRIPTION /from START_DATE_OR_TIME /to END_DATE_OR_TIME}.
     *
     * @param tasks tasks currently stored in memory
     * @param argument event description, start, and end
     * @throws KachowException if a required event component is invalid or missing
     */
    private static void addEvent(List<Task> tasks, String argument) throws KachowException {
        int fromIndex = findToken(argument, "/from", 0);
        int firstToIndex = findToken(argument, "/to", 0);
        if (argument.isEmpty() || fromIndex == 0) {
            throw new KachowException("This event racer needs a description. Use: " + EVENT_USAGE);
        }
        if (fromIndex == -1) {
            throw new KachowException("That event is missing its /from starting line. Use: " + EVENT_USAGE);
        }
        if (firstToIndex != -1 && firstToIndex < fromIndex) {
            throw new KachowException("That event's /from must come before /to. Use: " + EVENT_USAGE);
        }

        int toIndex = findToken(argument, "/to", fromIndex + 5);
        if (toIndex == -1) {
            throw new KachowException("That event is missing its /to finish line. Use: " + EVENT_USAGE);
        }
        if (findToken(argument, "/from", fromIndex + 5) != -1
                || findToken(argument, "/to", toIndex + 3) != -1) {
            throw new KachowException(
                    "That event has extra route markers. Use one /from and one /to: " + EVENT_USAGE);
        }

        String description = argument.substring(0, fromIndex).strip();
        String from = argument.substring(fromIndex + 5, toIndex).strip();
        String to = argument.substring(toIndex + 3).strip();
        if (description.isEmpty()) {
            throw new KachowException("This event racer needs a description. Use: " + EVENT_USAGE);
        }
        if (from.isEmpty()) {
            throw new KachowException("That event needs a start after /from. Use: " + EVENT_USAGE);
        }
        if (to.isEmpty()) {
            throw new KachowException("That event needs an end after /to. Use: " + EVENT_USAGE);
        }
        addTask(tasks, new Event(description, from, to));
    }

    /**
     * Locates a slash-prefixed syntax token when it appears as a complete word.
     *
     * @param text text to search
     * @param token token such as {@code /by}
     * @param startIndex index at which to begin searching
     * @return token index, or {@code -1} when no complete token exists
     */
    private static int findToken(String text, String token, int startIndex) {
        int tokenIndex = text.indexOf(token, startIndex);
        while (tokenIndex != -1) {
            int tokenEnd = tokenIndex + token.length();
            boolean startsWord = tokenIndex == 0 || Character.isWhitespace(text.charAt(tokenIndex - 1));
            boolean endsWord = tokenEnd == text.length() || Character.isWhitespace(text.charAt(tokenEnd));
            if (startsWord && endsWord) {
                return tokenIndex;
            }
            tokenIndex = text.indexOf(token, tokenIndex + 1);
        }
        return -1;
    }

    /**
     * Stores a task and prints a Lightning McQueen-themed confirmation.
     *
     * @param tasks tasks currently stored in memory
     * @param task task to add
     */
    private static void addTask(List<Task> tasks, Task task) {
        tasks.add(task);
        System.out.println(INDENT + "Ka-chow! A new racer joined the starting grid:");
        System.out.println(INDENT + "  " + task.getStatusText());
        String racerLabel = tasks.size() == 1 ? " racer" : " racers";
        System.out.println(INDENT + "Now you've got " + tasks.size() + racerLabel + " ready to roll.");
    }

    /**
     * Gets the task identified by a user-facing, 1-based task number.
     *
     * @param tasks tasks currently stored in memory
     * @param argument text containing the task number
     * @param action command being performed, used to make guidance specific
     * @return the selected task
     * @throws KachowException if the task number is missing, malformed, or outside the task list
     */
    private static Task getTask(List<Task> tasks, String argument, String action) throws KachowException {
        if (argument.isEmpty()) {
            throw new KachowException(
                    "Tell me which racer to " + action + ". Use: " + action + " TASK_NUMBER");
        }

        int taskNumber;
        try {
            taskNumber = Integer.parseInt(argument);
        } catch (NumberFormatException exception) {
            throw new KachowException(
                    "That racer number isn't a whole positive number. Use: " + action + " TASK_NUMBER",
                    exception);
        }
        if (taskNumber <= 0) {
            throw new KachowException(
                    "That racer number isn't a whole positive number. Use: " + action + " TASK_NUMBER");
        }
        if (taskNumber > tasks.size()) {
            throw new KachowException(
                    "Racer " + taskNumber + " isn't on the grid. Use list to check the task numbers.");
        }
        return tasks.get(taskNumber - 1);
    }

    /**
     * Marks the selected task as done and displays confirmation.
     *
     * @param tasks tasks currently stored in memory
     * @param argument text containing the task number
     * @throws KachowException if the task number does not identify a task
     */
    private static void handleMarkCommand(List<Task> tasks, String argument) throws KachowException {
        Task task = getTask(tasks, argument, "mark");
        task.markAsDone();
        printMarkedTask(task);
    }

    /**
     * Marks the selected task as not done and displays confirmation.
     *
     * @param tasks tasks currently stored in memory
     * @param argument text containing the task number
     * @throws KachowException if the task number does not identify a task
     */
    private static void handleUnmarkCommand(List<Task> tasks, String argument) throws KachowException {
        Task task = getTask(tasks, argument, "unmark");
        task.markAsNotDone();
        printUnmarkedTask(task);
    }

    /**
     * Displays a Lightning McQueen-themed validation message.
     *
     * @param message explanation of the error and how to correct it
     */
    private static void printError(String message) {
        System.out.println(INDENT + "Pit stop! " + message);
    }

    /**
     * Displays confirmation that a task was marked as done.
     *
     * @param task task that was marked
     */
    private static void printMarkedTask(Task task) {
        System.out.println(INDENT + "Ka-chow! This task crossed the finish line:");
        System.out.println(INDENT + "  " + task.getStatusText());
    }

    /**
     * Displays confirmation that a task was marked as not done.
     *
     * @param task task that was unmarked
     */
    private static void printUnmarkedTask(Task task) {
        System.out.println(INDENT + "Back to the starting grid! This task is not done yet:");
        System.out.println(INDENT + "  " + task.getStatusText());
    }
}
