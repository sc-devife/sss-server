package com.sss.app.bulkimport;

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
}
