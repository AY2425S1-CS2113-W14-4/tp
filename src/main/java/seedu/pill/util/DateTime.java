package seedu.pill.util;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateTime {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    DateTime() {}

    private static LocalDate getSystemDate() {
        return LocalDate.now();
    }

    private static LocalTime getSystemTime() {
        return LocalTime.now();
    }

    public static String getSystemDateString() {
        return getSystemDate().format(DATE_FORMATTER);
    }

    public static String getSystemTimeString() {
        return getSystemTime().format(TIME_FORMATTER);
    }

    public static String getSystemDateTimeString() {
        return getSystemDateString() + " " + getSystemTimeString();
    }
}
