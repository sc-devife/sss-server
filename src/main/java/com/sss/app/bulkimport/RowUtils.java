package com.sss.app.bulkimport;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

public final class RowUtils {
    private RowUtils() {}

    public static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    public static Integer parseIntOrNull(String value) {
        String v = blankToNull(value);
        if (v == null) return null;
        try {
            return Integer.parseInt(v);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static java.math.BigDecimal parseDecimalOrNull(String value) {
        String v = blankToNull(value);
        if (v == null) return null;
        try {
            return new java.math.BigDecimal(v);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // "YYYY-MM-DD" (same shape DatePicker/every other date field in this app
    // already uses) -> LocalDate, or null for blank/unparseable input.
    public static LocalDate parseLocalDateOrNull(String value) {
        String v = blankToNull(value);
        if (v == null) return null;
        try {
            return LocalDate.parse(v);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    // "HH:mm" (24-hour, same shape TimePicker uses) -> LocalTime, or null for
    // blank/unparseable input.
    public static LocalTime parseLocalTimeOrNull(String value) {
        String v = blankToNull(value);
        if (v == null) return null;
        try {
            return LocalTime.parse(v);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    // A single CSV cell holding several values (meal plan codes, room type
    // names, amenities, ...) — semicolon-separated so it doesn't collide with
    // the CSV's own comma delimiter. Blank entries and surrounding whitespace
    // are dropped; a blank cell yields an empty list, never null.
    public static List<String> splitList(String value) {
        String v = blankToNull(value);
        if (v == null) return List.of();
        return List.of(v.split(";")).stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
