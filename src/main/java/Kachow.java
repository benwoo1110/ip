/**
 * Starts the Kachow chatbot application.
 */
public class Kachow {
    private static final String DIVIDER = "____________________________________________________________";

    public static void main(String[] args) {
        String banner = " _  __          _                    \n"
                + "| |/ /__ _  ___| |__   _____      __\n"
                + "| ' // _` |/ __| '_ \\ / _ \\ \\ /\\ / /\n"
                + "| . \\ (_| | (__| | | | (_) \\ V  V / \n"
                + "|_|\\_\\__,_|\\___|_| |_|\\___/ \\_/\\_/  \n";

        System.out.println(DIVIDER);
        System.out.print(banner);
        System.out.println("Ka-chow! I'm Kachow, the fastest chatbot on the track.");
        System.out.println("What can I do for you before the next lap?");
        System.out.println(DIVIDER);
        System.out.println("Race complete! Catch you on the next lap. Ka-chow!");
        System.out.println(DIVIDER);
    }
}
