package com.benthecat.kachow.parser;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import org.junit.jupiter.api.Test;

/**
 * Tests the date and time formats exercised by the console UI test plan.
 */
class DateTimeParserTest {
    /** Verifies that every supported date syntax produces the expected calendar date. */
    @Test
    void parse_supportedDateFormats_returnExpectedDates() {
        assertAll(
                () -> assertEquals(LocalDate.of(2019, 10, 15), DateTimeParser.parse("2019-10-15").date()),
                () -> assertEquals(LocalDate.of(2019, 12, 2), DateTimeParser.parse("2/12/2019").date()),
                () -> assertEquals(LocalDate.of(2019, 12, 3), DateTimeParser.parse("2019/12/3").date()),
                () -> assertEquals(LocalDate.of(2019, 12, 3), DateTimeParser.parse("12/03/2019").date()));
    }

    /** Verifies supported clock formats and normalization of repeated whitespace. */
    @Test
    void parse_supportedTimesAndRepeatedWhitespace_returnExpectedDateTimes() {
        assertAll(
                () -> assertEquals(
                        LocalDateTime.of(2019, 12, 2, 18, 0),
                        DateTimeParser.parse("2/12/2019 1800").toLocalDateTime()),
                () -> assertEquals(
                        LocalDateTime.of(2020, 2, 29, 9, 30),
                        DateTimeParser.parse("2020/02/29 09:30").toLocalDateTime()),
                () -> assertEquals(
                        LocalDateTime.of(2024, 1, 1, 0, 0),
                        DateTimeParser.parse("2024-01-01 12am").toLocalDateTime()),
                () -> assertEquals(
                        LocalDateTime.of(2024, 1, 1, 18, 0),
                        DateTimeParser.parse("2024-01-01    6 PM").toLocalDateTime()));
    }

    /** Verifies that a time-only value inherits the supplied default date. */
    @Test
    void parse_timeOnlyWithDefaultDate_usesDefaultDate() {
        DateTimeParser.ParsedDateTime parsed =
                DateTimeParser.parse("10:30", LocalDate.of(2019, 12, 3));

        assertEquals(LocalDateTime.of(2019, 12, 3, 10, 30), parsed.toLocalDateTime());
    }

    /** Verifies that invalid calendar and clock values are rejected consistently. */
    @Test
    void parse_invalidDatesAndTimes_throwDateTimeParseException() {
        assertAll(
                () -> assertThrows(DateTimeParseException.class,
                        () -> DateTimeParser.parse("31/02/2019")),
                () -> assertThrows(DateTimeParseException.class,
                        () -> DateTimeParser.parse("2019-10-15 2460")),
                () -> assertThrows(DateTimeParseException.class,
                        () -> DateTimeParser.parse("tomorrow")));
    }

    /** Verifies stable human-readable and persistence date/time formats. */
    @Test
    void format_displayAndStorageFormats_areStable() {
        DateTimeParser.ParsedDateTime dateOnly = DateTimeParser.parse("2019-10-15");
        DateTimeParser.ParsedDateTime dateTime = DateTimeParser.parse("2019-12-02 1800");

        assertAll(
                () -> assertEquals("Oct 15 2019", DateTimeParser.format(dateOnly)),
                () -> assertEquals("Dec 02 2019, 6:00 PM", DateTimeParser.format(dateTime)),
                () -> assertEquals("2019-10-15", DateTimeParser.formatForStorage(dateOnly)),
                () -> assertEquals("2019-12-02T18:00", DateTimeParser.formatForStorage(dateTime)));
    }
}
