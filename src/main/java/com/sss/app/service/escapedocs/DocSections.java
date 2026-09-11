package com.sss.app.service.escapedocs;

/**
 * Which optional sections a generated Escape Document should include — the
 * Overview block is never gated by any of these, it always renders. Purely
 * an output-selection toggle: never affects which Escape/Itinerary/Quote
 * data is read.
 */
public record DocSections(
        boolean transports,
        boolean bankAccount,
        boolean itinerary,
        boolean inclusionsExclusions,
        boolean termsAndConditions
) {
}
