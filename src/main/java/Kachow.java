import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Starts the Kachow chatbot application.
 */
public class Kachow {
    private static final String INDENT = "    ";
    private static final String DIVIDER = "____________________________________________________________";

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
            String command = scanner.nextLine();
            int separatorIndex = command.indexOf(' ');
            String instruction = separatorIndex == -1 ? command : command.substring(0, separatorIndex);
            String argument = separatorIndex == -1 ? "" : command.substring(separatorIndex + 1).strip();

            System.out.println(INDENT + DIVIDER);
            switch (instruction) {
            case "bye" -> {
                System.out.println(INDENT + "Race complete! Catch you on the next lap. Ka-chow!");
                System.out.println(INDENT + DIVIDER);
                break commandLoop;
            }
            case "list" -> {
                System.out.println(INDENT + "Rev up! Here are the tasks in today's race:");
                for (int i = 0; i < tasks.size(); i++) {
                    System.out.println(INDENT + (i + 1) + "." + tasks.get(i).getStatusText());
                }
            }
            case "mark" -> handleMarkCommand(tasks, argument);
            case "unmark" -> handleUnmarkCommand(tasks, argument);
            case "todo" -> addTodo(tasks, argument);
            case "deadline" -> addDeadline(tasks, argument);
            case "event" -> addEvent(tasks, argument);
            default -> System.out.println(INDENT
                    + "That command took a wrong turn. Try todo, deadline, event, list, mark, or unmark.");
            }
            System.out.println(INDENT + DIVIDER);
        }
    }

    /**
     * Adds a todo task when its description is present.
     *
     * @param tasks tasks currently stored in memory
     * @param description text describing the todo
     */
    private static void addTodo(List<Task> tasks, String description) {
        if (description.isBlank()) {
            System.out.println(INDENT + "This racer needs a name. Use: todo DESCRIPTION");
            return;
        }
        addTask(tasks, new Todo(description));
    }

    /**
     * Parses and adds a deadline in the form {@code DESCRIPTION /by DATE_OR_TIME}.
     *
     * @param tasks tasks currently stored in memory
     * @param argument deadline description and due date or time
     */
    private static void addDeadline(List<Task> tasks, String argument) {
        String delimiter = " /by ";
        int byIndex = argument.indexOf(delimiter);
        if (byIndex <= 0 || byIndex + delimiter.length() >= argument.length()) {
            System.out.println(INDENT
                    + "That deadline missed its checkpoint. Use: deadline DESCRIPTION /by DATE_OR_TIME");
            return;
        }

        String description = argument.substring(0, byIndex).strip();
        String by = argument.substring(byIndex + delimiter.length()).strip();
        if (description.isEmpty() || by.isEmpty()) {
            System.out.println(INDENT
                    + "That deadline missed its checkpoint. Use: deadline DESCRIPTION /by DATE_OR_TIME");
            return;
        }
        addTask(tasks, new Deadline(description, by));
    }

    /**
     * Parses and adds an event in the form
     * {@code DESCRIPTION /from START_DATE_OR_TIME /to END_DATE_OR_TIME}.
     *
     * @param tasks tasks currently stored in memory
     * @param argument event description, start, and end
     */
    private static void addEvent(List<Task> tasks, String argument) {
        String fromDelimiter = " /from ";
        String toDelimiter = " /to ";
        int fromIndex = argument.indexOf(fromDelimiter);
        int toIndex = argument.indexOf(toDelimiter, fromIndex + fromDelimiter.length());
        boolean hasAllParts = fromIndex > 0
                && toIndex > fromIndex + fromDelimiter.length()
                && toIndex + toDelimiter.length() < argument.length();
        if (!hasAllParts) {
            System.out.println(INDENT
                    + "This event needs a full race route. Use: event DESCRIPTION /from START /to END");
            return;
        }

        String description = argument.substring(0, fromIndex).strip();
        String from = argument.substring(fromIndex + fromDelimiter.length(), toIndex).strip();
        String to = argument.substring(toIndex + toDelimiter.length()).strip();
        if (description.isEmpty() || from.isEmpty() || to.isEmpty()) {
            System.out.println(INDENT
                    + "This event needs a full race route. Use: event DESCRIPTION /from START /to END");
            return;
        }
        addTask(tasks, new Event(description, from, to));
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
     * Invalid and out-of-range numbers produce a helpful message instead of ending the program.
     *
     * @param tasks tasks currently stored in memory
     * @param argument text containing the task number
     * @return the selected task, or {@code null} if the number is invalid
     */
    private static Task getTask(List<Task> tasks, String argument) {
        int taskNumber;
        try {
            taskNumber = Integer.parseInt(argument);
        } catch (NumberFormatException exception) {
            System.out.println(INDENT + "I need a valid task number to make that pit stop.");
            return null;
        }

        int taskIndex = taskNumber - 1;
        if (taskIndex < 0 || taskIndex >= tasks.size()) {
            System.out.println(INDENT + "That task number isn't in the race. Check the list and try again.");
            return null;
        }
        return tasks.get(taskIndex);
    }

    /**
     * Marks the selected task as done and displays confirmation.
     *
     * @param tasks tasks currently stored in memory
     * @param argument text containing the task number
     */
    private static void handleMarkCommand(List<Task> tasks, String argument) {
        Task task = getTask(tasks, argument);
        if (task == null) {
            return;
        }
        task.markAsDone();
        printMarkedTask(task);
    }

    /**
     * Marks the selected task as not done and displays confirmation.
     *
     * @param tasks tasks currently stored in memory
     * @param argument text containing the task number
     */
    private static void handleUnmarkCommand(List<Task> tasks, String argument) {
        Task task = getTask(tasks, argument);
        if (task == null) {
            return;
        }
        task.markAsNotDone();
        printUnmarkedTask(task);
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
