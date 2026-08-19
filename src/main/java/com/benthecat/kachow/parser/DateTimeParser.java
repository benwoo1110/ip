package com.benthecat.kachow.parser;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Parses, displays, and serializes the date/time values used by task commands.
 */
public final class DateTimeParser {
    private static final DateTimeFormatter US_DATE_FORMAT = strictFormatter("M/d/uuuu");
    private static final Pattern PADDED_US_DATE_PATTERN = Pattern.compile("\\d{2}/\\d{2}/\\d{4}");
    private static final List<DateTimeFormatter> DATE_FORMATS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE.withResolverStyle(ResolverStyle.STRICT),
            strictFormatter("uuuu/M/d"),
            strictFormatter("d/M/uuuu"));
    private static final List<DateTimeFormatter> TIME_FORMATS = List.of(
            strictFormatter("HHmm"),
            strictFormatter("HH:mm"),
            strictFormatter("ha"),
            strictFormatter("h:mma"),
            strictFormatter("h a"),
            strictFormatter("h:mm a"));
    private static final DateTimeFormatter DATE_DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("MMM dd uuuu", Locale.ENGLISH);
    private static final DateTimeFormatter TIME_DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);

    private DateTimeParser() {
        // No instantiation for this utility class with only static methods.
    }

    /**
     * Parses a complete date with an optional time.
     *
     * @param text user-entered or stored date/time text
     * @return parsed date/time value
     * @throws DateTimeParseException if the text does not match a supported format
     */
    public static ParsedDateTime parse(String text) {
        return parse(text, null);
    }

    /**
     * Parses a date/time, allowing a time-only value when a default date is available.
     * This is useful for an event end time that occurs on its start date.
     *
     * @param text user-entered or stored date/time text
     * @param defaultDate date assigned to a time-only value, or {@code null} to require a date
     * @return parsed date/time value
     * @throws DateTimeParseException if the text does not match a supported format
     */
    public static ParsedDateTime parse(String text, LocalDate defaultDate) {
        String normalizedText = text.strip().replaceAll("\\s+", " ");
        try {
            return new ParsedDateTime(LocalDateTime.parse(normalizedText, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        } catch (DateTimeParseException ignored) {
            // User-entered date-times use a space instead of ISO's T separator.
        }

        int separatorIndex = normalizedText.indexOf(' ');
        if (separatorIndex != -1) {
            try {
                LocalDate date = parseDate(normalizedText.substring(0, separatorIndex));
                LocalTime time = parseTime(normalizedText.substring(separatorIndex + 1));
                return new ParsedDateTime(LocalDateTime.of(date, time));
            } catch (DateTimeParseException ignored) {
                // The complete value may still be a time-only AM/PM value such as "6 PM".
            }
        }

        try {
            return new ParsedDateTime(parseDate(normalizedText));
        } catch (DateTimeParseException ignored) {
            // A default date may allow this value to be parsed as a time only.
        }

        if (defaultDate != null) {
            try {
                return new ParsedDateTime(LocalDateTime.of(defaultDate, parseTime(normalizedText)));
            } catch (DateTimeParseException ignored) {
                // Report one consistent parse failure below.
            }
        }
        throw new DateTimeParseException("Unsupported date/time format", text, 0);
    }

    /**
     * Formats a parsed value for task-list display.
     *
     * @param value date/time value to display
     * @return value formatted as {@code MMM dd yyyy} with an optional time
     */
    public static String format(ParsedDateTime value) {
        String displayedValue = value.date().format(DATE_DISPLAY_FORMAT);
        if (value.time().isPresent()) {
            displayedValue += ", " + value.time().orElseThrow().format(TIME_DISPLAY_FORMAT);
        }
        return displayedValue;
    }

    /**
     * Formats a date for task-list display.
     *
     * @param date date to display
     * @return date formatted as {@code MMM dd yyyy}
     */
    public static String format(LocalDate date) {
        return format(new ParsedDateTime(date));
    }

    /**
     * Formats a parsed value in a stable ISO representation for persistence.
     *
     * @param value date/time value to serialize
     * @return ISO local date or date-time text
     */
    public static String formatForStorage(ParsedDateTime value) {
        return value.time()
                .map(time -> LocalDateTime.of(value.date(), time).toString())
                .orElseGet(() -> value.date().toString());
    }

    private static DateTimeFormatter strictFormatter(String pattern) {
        return new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendPattern(pattern)
                .toFormatter(Locale.ENGLISH)
                .withResolverStyle(ResolverStyle.STRICT);
    }

    private static LocalDate parseDate(String text) {
        if (PADDED_US_DATE_PATTERN.matcher(text).matches()) {
            try {
                return LocalDate.parse(text, US_DATE_FORMAT);
            } catch (DateTimeParseException ignored) {
                // A padded day-first date can still be valid when its first field exceeds 12.
            }
        }
        for (DateTimeFormatter formatter : DATE_FORMATS) {
            try {
                return LocalDate.parse(text, formatter);
            } catch (DateTimeParseException ignored) {
                // Try the next supported date format.
            }
        }
        try {
            return LocalDate.parse(text, US_DATE_FORMAT);
        } catch (DateTimeParseException ignored) {
            // Report one consistent parse failure below.
        }
        throw new DateTimeParseException("Unsupported date format", text, 0);
    }

    private static LocalTime parseTime(String text) {
        for (DateTimeFormatter formatter : TIME_FORMATS) {
            try {
                return LocalTime.parse(text, formatter);
            } catch (DateTimeParseException ignored) {
                // Try the next supported time format.
            }
        }
        throw new DateTimeParseException("Unsupported time format", text, 0);
    }

    /**
     * Holds a required calendar date and an optional time parsed from one command parameter.
     *
     * @param date parsed calendar date
     * @param time optional parsed time
     */
    public record ParsedDateTime(LocalDate date, Optional<LocalTime> time) {
        /**
         * Creates a validated parsed value.
         */
        public ParsedDateTime {
            Objects.requireNonNull(date);
            Objects.requireNonNull(time);
        }

        /**
         * Creates a date-only value.
         *
         * @param date parsed calendar date
         */
        public ParsedDateTime(LocalDate date) {
            this(date, Optional.empty());
        }

        /**
         * Creates a value containing both a date and time.
         *
         * @param dateTime parsed date and time
         */
        public ParsedDateTime(LocalDateTime dateTime) {
            this(dateTime.toLocalDate(), Optional.of(dateTime.toLocalTime()));
        }

        /**
         * Converts this value to a date-time, treating a date without a time as midnight.
         *
         * @return equivalent date-time suitable for chronological comparisons
         */
        public LocalDateTime toLocalDateTime() {
            return LocalDateTime.of(date, time.orElse(LocalTime.MIN));
        }
    }
}
