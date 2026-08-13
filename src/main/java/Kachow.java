import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Starts the Kachow chatbot application.
 */
public class Kachow {
    private static final String INDENT = "    ";
    private static final String DIVIDER = "____________________________________________________________";

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
        Scanner scanner = new Scanner(System.in);
        commandLoop:
        while (scanner.hasNextLine()) {
            String command = scanner.nextLine();
            System.out.println(INDENT + DIVIDER);
            switch (command) {
            case "bye" -> {
                System.out.println(INDENT + "Race complete! Catch you on the next lap. Ka-chow!");
                System.out.println(INDENT + DIVIDER);
                break commandLoop;
            }
            case "list" -> {
                for (int i = 0; i < tasks.size(); i++) {
                    System.out.println(INDENT + (i + 1) + ". " + tasks.get(i));
                }
            }
            default -> {
                tasks.add(command);
                System.out.println(INDENT + "added: " + command);
            }
            }
            System.out.println(INDENT + DIVIDER);
        }
    }
}
