package com.benthecat.kachow.parser;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.EnumMap;
import java.util.Map;

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
    private static final String USAGE_EDIT = "edit TASK_NUMBER FIELD VALUE [FIELD VALUE]...";
    private static final String USAGE_FIND = "find KEYWORD";
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
        if (input == null) {
            throw new KachowException("Enter a command to keep racing.");
        }
        if (input.codePoints().anyMatch(character -> (
                Character.isISOControl(character) && character != '\t')
                        || character == 0x2028 || character == 0x2029)) {
            throw new KachowException("Enter one command without line breaks or control characters.");
        }

        String commandText = input.replaceAll("(?U)\\s+", " ").strip();
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
        assert parsedCommand.command() == Command.BYE || parsedCommand.command() == Command.LIST
                : "Only bye and list are argument-free commands";

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
        assert parsedCommand.command() == Command.TODO
                || parsedCommand.command() == Command.DEADLINE
                || parsedCommand.command() == Command.EVENT
                : "Only task-creation commands can be parsed as tasks";

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
        return new Todo(validateDescription(description));
    }

    /** Validates descriptions before constructing a task and explains unsupported field markers. */
    private String validateDescription(String description) throws KachowException {
        try {
            String normalizedDescription = Task.normalizeDescription(description);
            if (findNextEditFieldIndex(normalizedDescription, 0) != -1) {
                throw new KachowException(
                        "Descriptions cannot contain slash-prefixed fields. Check the command's parameters.");
            }
            return normalizedDescription;
        } catch (IllegalArgumentException exception) {
            throw new KachowException(exception.getMessage(), exception);
        }
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

        String description = validateDescription(argument.substring(0, byIndex).strip());
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

        String description = validateDescription(argument.substring(0, fromIndex).strip());
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
                    "That event must end after it starts. Use a full /to date for an overnight event.",
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
        assert parsedCommand.command() == Command.ON : "Only the on command has a date argument";

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
     * Returns the non-empty keyword accepted by the {@code find} command.
     *
     * @param parsedCommand Find command to parse.
     * @return Keyword to search for in task descriptions.
     * @throws KachowException If the keyword is missing.
     */
    public String parseSearchKeyword(ParsedCommand parsedCommand) throws KachowException {
        assert parsedCommand.command() == Command.FIND : "Only the find command has a search keyword";

        String keyword = parsedCommand.argument();
        if (keyword.isEmpty()) {
            throw new KachowException("Tell me which racer to search for. Use: " + USAGE_FIND);
        }
        return keyword;
    }

    /**
     * Parses a positive, one-based task number for a task-list command.
     *
     * @param parsedCommand Mark, unmark, or delete command to parse.
     * @return Validated task number.
     * @throws KachowException If the task number is missing or malformed.
     */
    public int parseTaskNumber(ParsedCommand parsedCommand) throws KachowException {
        assert parsedCommand.command() == Command.MARK
                || parsedCommand.command() == Command.UNMARK
                || parsedCommand.command() == Command.DELETE
                : "Only task-list commands have a task number";

        String argument = parsedCommand.argument();
        Command action = parsedCommand.command();
        if (argument.isEmpty()) {
            throw new KachowException(
                    "Tell me which racer to " + action.getKeyword()
                            + ". Use: " + action.getKeyword() + " TASK_NUMBER");
        }

        return parsePositiveTaskNumber(argument, action);
    }

    /**
     * Parses the task number and replacement field/value pairs of an edit command.
     *
     * @param parsedCommand Edit command to parse.
     * @return Validated edit request.
     * @throws KachowException If the task number, a field, or a replacement value is missing or malformed.
     */
    public EditCommand parseEditCommand(ParsedCommand parsedCommand) throws KachowException {
        assert parsedCommand.command() == Command.EDIT : "Only the edit command can be parsed as an edit";

        String argument = parsedCommand.argument();
        if (argument.isEmpty()) {
            throw new KachowException("Tell me which racer to edit. Use: " + USAGE_EDIT);
        }

        int numberEndIndex = findFirstWhitespaceIndex(argument);
        String taskNumberText = numberEndIndex == -1 ? argument : argument.substring(0, numberEndIndex);
        int taskNumber = parsePositiveTaskNumber(taskNumberText, Command.EDIT);
        if (numberEndIndex == -1) {
            throw createMissingEditFieldException();
        }

        String fieldArguments = argument.substring(numberEndIndex).strip();
        return new EditCommand(taskNumber, parseEditChanges(fieldArguments));
    }

    /**
     * Creates an updated copy of a task according to one parsed edit request.
     *
     * @param task Existing task to update.
     * @param editCommand Parsed edit request.
     * @return Updated task with its unedited details and completion status preserved.
     * @throws KachowException If the selected field does not apply or its new value is invalid.
     */
    public Task applyEdit(Task task, EditCommand editCommand) throws KachowException {
        assert task != null : "Edited task must not be null";
        assert editCommand != null : "Edit command must not be null";

        String description = validateDescription(editCommand.changes()
                .getOrDefault(EditField.DESCRIPTION, task.getDescription()));
        return switch (task) {
            case Todo todo -> editTodo(todo, description, editCommand);
            case Deadline deadline -> editDeadline(deadline, description, editCommand);
            case Event event -> editEvent(event, description, editCommand);
            default -> throw new IllegalArgumentException("Unsupported task type: " + task.getClass().getName());
        };
    }

    /** Parses every field/value pair while rejecting duplicate or incomplete fields. */
    private Map<EditField, String> parseEditChanges(String fieldArguments) throws KachowException {
        Map<EditField, String> changes = new EnumMap<>(EditField.class);
        int fieldIndex = 0;
        while (fieldIndex < fieldArguments.length()) {
            int fieldEndIndex = findFirstWhitespaceIndex(fieldArguments.substring(fieldIndex));
            int absoluteFieldEndIndex = fieldEndIndex == -1
                    ? fieldArguments.length()
                    : fieldIndex + fieldEndIndex;
            String fieldText = fieldArguments.substring(fieldIndex, absoluteFieldEndIndex);
            EditField field = EditField.fromKeyword(fieldText);
            if (changes.containsKey(field)) {
                throw new KachowException(
                        "That edit repeats " + field.getKeyword() + ". Specify each detail once.");
            }

            int valueStartIndex = skipWhitespace(fieldArguments, absoluteFieldEndIndex);
            int nextFieldIndex = findNextEditFieldIndex(fieldArguments, valueStartIndex);
            int valueEndIndex = nextFieldIndex == -1 ? fieldArguments.length() : nextFieldIndex;
            String value = fieldArguments.substring(valueStartIndex, valueEndIndex).strip();
            if (value.isEmpty()) {
                throw new KachowException(
                        "That " + field.getKeyword() + " detail needs a new value. Use: " + USAGE_EDIT);
            }
            changes.put(field, value);
            fieldIndex = valueEndIndex;
        }
        return Map.copyOf(changes);
    }

    /** Parses a positive task number from a complete numeric token. */
    private int parsePositiveTaskNumber(String taskNumberText, Command action) throws KachowException {
        if (!taskNumberText.matches("[0-9]+")) {
            throw createInvalidTaskNumberException(action, null);
        }
        int taskNumber;
        try {
            taskNumber = Integer.parseInt(taskNumberText);
        } catch (NumberFormatException exception) {
            throw createInvalidTaskNumberException(action, exception);
        }
        if (taskNumber <= 0) {
            throw createInvalidTaskNumberException(action, null);
        }
        return taskNumber;
    }

    /** Creates an edited todo after ensuring that every requested field is supported. */
    private Task editTodo(Todo todo, String description, EditCommand editCommand) throws KachowException {
        requireSupportedFields(editCommand, EditField.DESCRIPTION);
        return new Todo(description, todo.isDone());
    }

    /** Creates an edited deadline after ensuring that every requested field is supported. */
    private Task editDeadline(Deadline deadline, String description, EditCommand editCommand)
            throws KachowException {
        requireSupportedFields(editCommand, EditField.DESCRIPTION, EditField.BY);

        DateTimeParser.ParsedDateTime by = deadline.getByValue();
        if (editCommand.changes().containsKey(EditField.BY)) {
            by = parseEditedDeadlineDateTime(editCommand.changes().get(EditField.BY), by.date());
        }
        return new Deadline(description, by, deadline.isDone());
    }

    /** Parses an edited deadline value, allowing a time that keeps the existing date. */
    private DateTimeParser.ParsedDateTime parseEditedDeadlineDateTime(String value, LocalDate existingDate)
            throws KachowException {
        try {
            return DateTimeParser.parse(value, existingDate);
        } catch (DateTimeParseException exception) {
            throw new KachowException(
                    "That deadline date or time is invalid. " + DATE_TIME_FORMAT_GUIDANCE,
                    exception);
        }
    }

    /** Creates an edited event after parsing all requested values and validating the final range. */
    private Task editEvent(Event event, String description, EditCommand editCommand) throws KachowException {
        requireSupportedFields(editCommand, EditField.DESCRIPTION, EditField.FROM, EditField.TO);

        DateTimeParser.ParsedDateTime from = event.getFrom();
        if (editCommand.changes().containsKey(EditField.FROM)) {
            from = parseEditedEventDateTime(
                    editCommand.changes().get(EditField.FROM), event.getFrom().date(), "start");
        }
        DateTimeParser.ParsedDateTime to = event.getTo();
        if (editCommand.changes().containsKey(EditField.TO)) {
            to = parseEditedEventDateTime(
                    editCommand.changes().get(EditField.TO), event.getTo().date(), "end");
        }

        try {
            return new Event(description, from, to, event.isDone());
        } catch (IllegalArgumentException exception) {
            throw new KachowException(
                    "That event must end after it starts. Use a full date when moving it across midnight.",
                    exception);
        }
    }

    /** Parses an edited event value, allowing a time that keeps the existing field's date. */
    private DateTimeParser.ParsedDateTime parseEditedEventDateTime(String value, LocalDate existingDate,
            String parameter) throws KachowException {
        try {
            return DateTimeParser.parse(value, existingDate);
        } catch (DateTimeParseException exception) {
            throw createInvalidEventDateTimeException(parameter, exception);
        }
    }

    /** Rejects the first requested field that is unsupported by the selected task type. */
    private void requireSupportedFields(EditCommand editCommand, EditField... supportedFields)
            throws KachowException {
        for (EditField field : EditField.values()) {
            if (editCommand.changes().containsKey(field) && !isSupportedField(field, supportedFields)) {
                throw new KachowException("This racer does not have a " + field.getKeyword() + " detail.");
            }
        }
    }

    /** Reports whether a field is in the selected task type's supported field list. */
    private boolean isSupportedField(EditField field, EditField[] supportedFields) {
        for (EditField supportedField : supportedFields) {
            if (field == supportedField) {
                return true;
            }
        }
        return false;
    }

    /** Creates guidance for an edit command that has no field. */
    private KachowException createMissingEditFieldException() {
        return new KachowException(
                "Tell me what to change. Use /description, /by, /from, or /to: " + USAGE_EDIT);
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

    /** Skips whitespace at and after an index. */
    private int skipWhitespace(String text, int startIndex) {
        int index = startIndex;
        while (index < text.length() && Character.isWhitespace(text.charAt(index))) {
            index++;
        }
        return index;
    }

    /** Finds the next slash-prefixed edit field that begins after whitespace. */
    private int findNextEditFieldIndex(String text, int startIndex) {
        for (int i = startIndex; i < text.length(); i++) {
            if (text.charAt(i) == '/' && (i == 0 || Character.isWhitespace(text.charAt(i - 1)))) {
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

    /** Holds a task number and every replacement field/value pair of an edit command. */
    public record EditCommand(int taskNumber, Map<EditField, String> changes) {
        /** Creates an edit command with an immutable snapshot of its changes. */
        public EditCommand {
            changes = Map.copyOf(changes);
        }
    }

    /** Identifies a task detail that the edit command can replace. */
    public enum EditField {
        DESCRIPTION("/description"),
        BY("/by"),
        FROM("/from"),
        TO("/to");

        private final String keyword;

        EditField(String keyword) {
            this.keyword = keyword;
        }

        /** Returns the slash-prefixed keyword for this editable field. */
        public String getKeyword() {
            return keyword;
        }

        /** Converts a slash-prefixed field keyword into its corresponding editable field. */
        private static EditField fromKeyword(String keyword) throws KachowException {
            for (EditField field : values()) {
                if (field.keyword.equals(keyword)) {
                    return field;
                }
            }
            throw new KachowException(
                    "That detail cannot be edited. Use /description, /by, /from, or /to.");
        }
    }
}
