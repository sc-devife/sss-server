package com.sss.app.service.reminder;

import java.util.List;

/**
 * Section 8's recommended default cadence — used whenever an org hasn't
 * configured its own reminder rules yet, so reminders work out of the box
 * without every org needing to set this up first. Shape mirrors
 * ReminderRule so the job's matching logic doesn't need two code paths.
 */
public record DefaultReminderCadence(String label, int offsetDays, boolean recurring) {

    public static final List<DefaultReminderCadence> RULES = List.of(
            new DefaultReminderCadence("7 days before due", -7, false),
            new DefaultReminderCadence("1 day before due", -1, false),
            new DefaultReminderCadence("On the due date", 0, false),
            new DefaultReminderCadence("Every 2 days while overdue", 2, true)
    );
}
