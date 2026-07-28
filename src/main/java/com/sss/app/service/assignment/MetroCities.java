package com.sss.app.service.assignment;

import java.util.Set;

/**
 * Section 5 Excel writeup: "Region: Lead is from Metro cities" is one of the
 * auto-priority criteria. Fixed list rather than admin-configurable (unlike
 * the Priority Calendar) — the Excel doesn't call out per-org customization
 * for this one. Matched case-insensitively against Lead.originCity.
 */
public final class MetroCities {

    private MetroCities() {}

    public static final Set<String> NAMES = Set.of(
            "mumbai", "delhi", "new delhi", "bengaluru", "bangalore",
            "chennai", "kolkata", "hyderabad", "pune", "ahmedabad"
    );

    public static boolean isMetro(String city) {
        if (city == null || city.isBlank()) return false;
        return NAMES.contains(city.trim().toLowerCase());
    }
}
