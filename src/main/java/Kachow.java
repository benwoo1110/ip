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

        List<String> tasks = new ArrayList<>();
        List<Boolean> taskStatuses = new ArrayList<>();
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
                    String statusIcon = taskStatuses.get(i) ? "[X] " : "[ ] ";
                    System.out.println(INDENT + (i + 1) + "." + statusIcon + tasks.get(i));
                }
            }
            case "mark" -> handleMarkCommand(tasks, taskStatuses, argument);
            case "unmark" -> handleUnmarkCommand(tasks, taskStatuses, argument);
            default -> {
                tasks.add(command);
                taskStatuses.add(false);
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
     * @return the zero-based index of the selected task, or {@code -1} if the number is invalid
     */
    private static int getTaskIndex(List<String> tasks, String argument) {
        int taskNumber;
        try {
            taskNumber = Integer.parseInt(argument);
        } catch (NumberFormatException exception) {
            System.out.println(INDENT + "I need a valid task number to make that pit stop.");
            return -1;
        }

        int taskIndex = taskNumber - 1;
        if (taskIndex < 0 || taskIndex >= tasks.size()) {
            System.out.println(INDENT + "That task number isn't in the race. Check the list and try again.");
            return -1;
        }
        return taskIndex;
    }

    /**
     * Marks the selected task as done and displays confirmation.
     *
     * @param tasks tasks currently stored in memory
     * @param taskStatuses completion status corresponding to each task
     * @param argument text containing the task number
     */
    private static void handleMarkCommand(List<String> tasks, List<Boolean> taskStatuses, String argument) {
        int taskIndex = getTaskIndex(tasks, argument);
        if (taskIndex == -1) {
            return;
        }
        taskStatuses.set(taskIndex, true);
        printMarkedTask(tasks.get(taskIndex));
    }

    /**
     * Marks the selected task as not done and displays confirmation.
     *
     * @param tasks tasks currently stored in memory
     * @param taskStatuses completion status corresponding to each task
     * @param argument text containing the task number
     */
    private static void handleUnmarkCommand(List<String> tasks, List<Boolean> taskStatuses, String argument) {
        int taskIndex = getTaskIndex(tasks, argument);
        if (taskIndex == -1) {
            return;
        }
        taskStatuses.set(taskIndex, false);
        printUnmarkedTask(tasks.get(taskIndex));
    }

    /**
     * Displays confirmation that a task was marked as done.
     *
     * @param taskDescription description of the task that was marked
     */
    private static void printMarkedTask(String taskDescription) {
        System.out.println(INDENT + "Ka-chow! This task crossed the finish line:");
        System.out.println(INDENT + "  [X] " + taskDescription);
    }

    /**
     * Displays confirmation that a task was marked as not done.
     *
     * @param taskDescription description of the task that was unmarked
     */
    private static void printUnmarkedTask(String taskDescription) {
        System.out.println(INDENT + "Back to the starting grid! This task is not done yet:");
        System.out.println(INDENT + "  [ ] " + taskDescription);
    }
}
