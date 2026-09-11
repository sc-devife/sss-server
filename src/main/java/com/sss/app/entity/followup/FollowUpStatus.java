package com.sss.app.entity.followup;

import java.util.List;

public final class FollowUpStatus {
    private FollowUpStatus() {}

    public static final String PENDING = "Pending";
    public static final String HOLD = "Hold";
    public static final String COMPLETED = "Completed";

    public static final List<String> ALL = List.of(PENDING, HOLD, COMPLETED);
}
