package com.sss.app.entity.lead;

import java.util.List;

/** Section 7 lifecycle constants, shared between lifecycle transitions and the assignment engine's open-lead counting. */
public final class LeadStatus {
    private LeadStatus() {}

    public static final String NEW = "New";
    public static final String CONTACTED = "Contacted";
    public static final String QUALIFIED = "Qualified";
    public static final String CONVERTED = "Converted";
    public static final String UNQUALIFIED = "Unqualified";
    public static final String LOST = "Lost";
    public static final String DUPLICATE = "Duplicate";

    public static final List<String> TERMINAL = List.of(UNQUALIFIED, LOST, DUPLICATE, CONVERTED);
}
