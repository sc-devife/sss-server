package com.sss.app.service.followup;

public interface FollowUpReminderService {
    /** Sweeps for open actionable follow-ups due in ~30 minutes; returns count sent. */
    int sendThirtyMinuteReminders();

    /** Sweeps for open actionable follow-ups due in ~15 minutes; returns count sent. */
    int sendFifteenMinuteReminders();
}
