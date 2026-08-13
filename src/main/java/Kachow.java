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
            default -> {
                tasks.add(new Task(command));
                System.out.println(INDENT + "Added to the race lineup: " + command);
            }
            }
            System.out.println(INDENT + DIVIDER);
        }
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
