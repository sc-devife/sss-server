package com.sss.app.service.reminder;

import com.sss.app.dto.reminder.ReminderRuleCreateRequestDTO;
import com.sss.app.dto.reminder.ReminderRuleResponseDTO;

import java.util.List;
import java.util.UUID;

public interface PaymentReminderService {
    List<ReminderRuleResponseDTO> getRulesForOrg();
    ReminderRuleResponseDTO createRule(ReminderRuleCreateRequestDTO request);
    void deleteRule(UUID uid);

    /** Runs the whole reminder sweep once — invoked by the scheduled job, exposed so it can also be triggered manually for testing. */
    int runDailyReminders();
}
