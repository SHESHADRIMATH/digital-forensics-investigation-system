package com.digitalforensics.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/** JSTL cannot format java.time objects, so models expose pre-formatted strings via this class. */
public final class DateUtil {

    public static final DateTimeFormatter DISPLAY = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
    public static final DateTimeFormatter DISPLAY_SECONDS = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm:ss");
    /** Format used by <input type="datetime-local">. */
    public static final DateTimeFormatter HTML_INPUT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    private DateUtil() {
    }

    public static String format(LocalDateTime time) {
        return time == null ? "-" : time.format(DISPLAY);
    }

    public static String formatSeconds(LocalDateTime time) {
        return time == null ? "-" : time.format(DISPLAY_SECONDS);
    }

    /** Parses a datetime-local form value ("2026-09-19T22:41" or with ":10" seconds); null if blank/invalid. */
    public static LocalDateTime parseInput(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return LocalDateTime.parse(value.trim()); // ISO format, seconds optional
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public static String nowForInput() {
        return LocalDateTime.now().format(HTML_INPUT);
    }
}
