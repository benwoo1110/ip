package com.benthecat.kachow.parser;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import com.benthecat.kachow.exception.KachowException;
import com.benthecat.kachow.task.Deadline;
import com.benthecat.kachow.task.Event;
import com.benthecat.kachow.task.Task;
import com.benthecat.kachow.task.Todo;

/**
 * Converts raw user input into validated commands that the application can execute.
 */
public class Parser {
    private static final String USAGE_DEADLINE = "deadline DESCRIPTION /by DATE_OR_TIME";
    private static final String USAGE_EVENT = "event DESCRIPTION /from START /to END";
    private static final String USAGE_ON = "on DATE";
    private static final String DATE_FORMATS_TEXT =
            "yyyy-MM-dd, yyyy/M/d, d/M/yyyy, or padded MM/dd/yyyy (US)";
    private static final String DATE_FORMAT_GUIDANCE = "Use " + DATE_FORMATS_TEXT + ".";
    private static final String DATE_TIME_FORMAT_GUIDANCE =
            "Use " + DATE_FORMATS_TEXT + ", optionally followed by HHmm, HH:mm, or an AM/PM time.";

    /** Creates a parser for Kachow's supported console commands. */
    public Parser() {
        // This parser has no mutable state to initialize.
    }

    /**
     * Separates one input line into its command and argument.
     *
     * @param input Raw input entered by the user.
     * @return Parsed command keyword and its remaining argument.
     * @throws KachowException If the command keyword is empty or unsupported.
     */
    public ParsedCommand parse(String input) throws KachowException {
        String commandText = input.strip();
        int separatorIndex = findFirstWhitespaceIndex(commandText);
        String keyword = separatorIndex == -1 ? commandText : commandText.substring(0, separatorIndex);
        String argument = separatorIndex == -1 ? "" : commandText.substring(separatorIndex).strip();

        if (keyword.isEmpty()) {
            throw new KachowException(
                    "That command stalled on the starting line. Enter a command to keep racing.");
        }

        return new ParsedCommand(Command.fromKeyword(keyword), argument);
    }

    /**
     * Validates a command that must not have an argument.
     *
     * @param parsedCommand Command to validate.
     * @throws KachowException If an unexpected argument is present.
     */
    public void requireNoArgument(ParsedCommand parsedCommand) throws KachowException {
        if (!parsedCommand.argument().isEmpty()) {
            Command command = parsedCommand.command();
            throw new KachowException(
                    "The " + command.getKeyword() + " command has extra cargo. Use: " + command.getKeyword());
        }
    }

    /**
     * Creates the task described by an add command.
     *
     * @param parsedCommand Todo, deadline, or event command to parse.
     * @return Parsed task.
     * @throws KachowException If a required task component is missing or invalid.
     */
    public Task parseTask(ParsedCommand parsedCommand) throws KachowException {
        return switch (parsedCommand.command()) {
        case TODO -> parseTodo(parsedCommand.argument());
        case DEADLINE -> parseDeadline(parsedCommand.argument());
        case EVENT -> parseEvent(parsedCommand.argument());
        default -> throw new IllegalArgumentException(
                "Command does not create a task: " + parsedCommand.command());
        };
    }

    /** Creates a todo after ensuring that it has a description. */
    private Todo parseTodo(String description) throws KachowException {
        if (description.isBlank()) {
            throw new KachowException("This racer needs a name. Use: todo DESCRIPTION");
        }
        return new Todo(description);
    }

    /** Parses a deadline in the form {@code DESCRIPTION /by DATE_OR_TIME}. */
    private Deadline parseDeadline(String argument) throws KachowException {
        int byIndex = findToken(argument, "/by", 0);
        if (argument.isEmpty() || byIndex == 0) {
            throw new KachowException(
                    "This deadline racer needs a task description. Use: " + USAGE_DEADLINE);
        }
        if (byIndex == -1) {
            throw new KachowException("That deadline is missing its /by checkpoint. Use: " + USAGE_DEADLINE);
        }
        if (findToken(argument, "/by", byIndex + 3) != -1) {
            throw new KachowException(
                    "That deadline has too many /by checkpoints. Use exactly one: " + USAGE_DEADLINE);
        }

        String description = argument.substring(0, byIndex).strip();
        String by = argument.substring(byIndex + 3).strip();
        if (description.isEmpty()) {
            throw new KachowException(
                    "This deadline racer needs a task description. Use: " + USAGE_DEADLINE);
        }
        if (by.isEmpty()) {
            throw new KachowException(
                    "That deadline needs a date or time after /by. Use: " + USAGE_DEADLINE);
        }

        try {
            return new Deadline(description, DateTimeParser.parse(by));
        } catch (DateTimeParseException exception) {
            throw new KachowException(
                    "That deadline date or time is invalid. " + DATE_TIME_FORMAT_GUIDANCE,
                    exception);
        }
    }

    /** Parses an event in the form {@code DESCRIPTION /from START /to END}. */
    private Event parseEvent(String argument) throws KachowException {
        int fromIndex = findToken(argument, "/from", 0);
        int firstToIndex = findToken(argument, "/to", 0);
        if (argument.isEmpty() || fromIndex == 0) {
            throw new KachowException("This event racer needs a description. Use: " + USAGE_EVENT);
        }
        if (fromIndex == -1) {
            throw new KachowException("That event is missing its /from starting line. Use: " + USAGE_EVENT);
        }
        if (firstToIndex != -1 && firstToIndex < fromIndex) {
            throw new KachowException("That event's /from must come before /to. Use: " + USAGE_EVENT);
        }

        int toIndex = findToken(argument, "/to", fromIndex + 5);
        if (toIndex == -1) {
            throw new KachowException("That event is missing its /to finish line. Use: " + USAGE_EVENT);
        }
        if (findToken(argument, "/from", fromIndex + 5) != -1
                || findToken(argument, "/to", toIndex + 3) != -1) {
            throw new KachowException(
                    "That event has extra route markers. Use one /from and one /to: " + USAGE_EVENT);
        }

        String description = argument.substring(0, fromIndex).strip();
        String from = argument.substring(fromIndex + 5, toIndex).strip();
        String to = argument.substring(toIndex + 3).strip();
        if (description.isEmpty()) {
            throw new KachowException("This event racer needs a description. Use: " + USAGE_EVENT);
        }
        if (from.isEmpty()) {
            throw new KachowException("That event needs a start after /from. Use: " + USAGE_EVENT);
        }
        if (to.isEmpty()) {
            throw new KachowException("That event needs an end after /to. Use: " + USAGE_EVENT);
        }

        DateTimeParser.ParsedDateTime parsedFrom;
        try {
            parsedFrom = DateTimeParser.parse(from);
        } catch (DateTimeParseException exception) {
            throw createInvalidEventDateTimeException("start", exception);
        }

        DateTimeParser.ParsedDateTime parsedTo;
        try {
            parsedTo = DateTimeParser.parse(to, parsedFrom.date());
        } catch (DateTimeParseException exception) {
            throw createInvalidEventDateTimeException("end", exception);
        }

        try {
            return new Event(description, parsedFrom, parsedTo);
        } catch (IllegalArgumentException exception) {
            throw new KachowException(
                    "That event ends before it starts. Use a full /to date for an overnight event.",
                    exception);
        }
    }

    /**
     * Parses the date argument accepted by the {@code on} command.
     *
     * @param parsedCommand On command to parse.
     * @return Validated calendar date.
     * @throws KachowException If the date is missing, invalid, or includes a time.
     */
    public LocalDate parseDate(ParsedCommand parsedCommand) throws KachowException {
        String argument = parsedCommand.argument();
        if (argument.isEmpty()) {
            throw new KachowException("Tell me which race date to check. Use: " + USAGE_ON);
        }

        DateTimeParser.ParsedDateTime parsedDate;
        try {
            parsedDate = DateTimeParser.parse(argument);
        } catch (DateTimeParseException exception) {
            throw new KachowException("That date is invalid. " + DATE_FORMAT_GUIDANCE, exception);
        }
        if (parsedDate.time().isPresent()) {
            throw new KachowException("The on command needs a date without a time. Use: " + USAGE_ON);
        }
        return parsedDate.date();
    }

    /**
     * Parses a positive, one-based task number for a task-list command.
     *
     * @param parsedCommand Mark, unmark, or delete command to parse.
     * @return Validated task number.
     * @throws KachowException If the task number is missing or malformed.
     */
    public int parseTaskNumber(ParsedCommand parsedCommand) throws KachowException {
        String argument = parsedCommand.argument();
        Command action = parsedCommand.command();
        if (argument.isEmpty()) {
            throw new KachowException(
                    "Tell me which racer to " + action.getKeyword()
                            + ". Use: " + action.getKeyword() + " TASK_NUMBER");
        }

        int taskNumber;
        try {
            taskNumber = Integer.parseInt(argument);
        } catch (NumberFormatException exception) {
            throw createInvalidTaskNumberException(action, exception);
        }
        if (taskNumber <= 0) {
            throw createInvalidTaskNumberException(action, null);
        }
        return taskNumber;
    }

    /** Creates consistent guidance for malformed task numbers. */
    private KachowException createInvalidTaskNumberException(Command action, NumberFormatException cause) {
        String message = "That racer number isn't a whole positive number. Use: "
                + action.getKeyword() + " TASK_NUMBER";
        return cause == null ? new KachowException(message) : new KachowException(message, cause);
    }

    /** Creates consistent, parameter-specific guidance for an invalid event date/time. */
    private KachowException createInvalidEventDateTimeException(String parameter, DateTimeParseException cause) {
        return new KachowException(
                "That event " + parameter + " date or time is invalid. " + DATE_TIME_FORMAT_GUIDANCE,
                cause);
    }

    /** Finds the first whitespace character so repeated spaces and tabs are accepted. */
    private int findFirstWhitespaceIndex(String text) {
        for (int i = 0; i < text.length(); i++) {
            if (Character.isWhitespace(text.charAt(i))) {
                return i;
            }
        }
        return -1;
    }

    /** Locates a slash-prefixed syntax token when it appears as a complete word. */
    private int findToken(String text, String token, int startIndex) {
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

    /** Holds a recognized command and the unprocessed text following its keyword. */
    public record ParsedCommand(Command command, String argument) { }
}
