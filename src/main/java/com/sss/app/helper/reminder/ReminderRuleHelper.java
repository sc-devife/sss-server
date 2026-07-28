package com.sss.app.helper.reminder;

import com.sss.app.dto.reminder.ReminderRuleCreateRequestDTO;
import com.sss.app.entity.reminder.ReminderRule;
import com.sss.app.entity.users.User;
import com.sss.app.exception.NotFoundException;
import com.sss.app.repository.reminder.ReminderRuleRepository;
import com.sss.app.security.OrgAccessGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReminderRuleHelper {

    private final ReminderRuleRepository reminderRuleRepository;
    private final OrgAccessGuard orgAccessGuard;

    private User currentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public ReminderRule create(ReminderRuleCreateRequestDTO request) {
        ReminderRule rule = ReminderRule.builder()
                .orgId(currentUser().getOrgId())
                .label(request.getLabel())
                .offsetDays(request.getOffsetDays())
                .recurring(request.getRecurring() != null && request.getRecurring())
                .isActive(true)
                .build();
        return reminderRuleRepository.save(rule);
    }

    public List<ReminderRule> getAllForOrg() {
        return reminderRuleRepository.findAllByOrgId(currentUser().getOrgId());
    }

    public void delete(UUID uid) {
        ReminderRule rule = reminderRuleRepository.findByUid(uid)
                .orElseThrow(() -> new NotFoundException("Reminder rule not found"));
        orgAccessGuard.requireAccessToOrg(rule.getOrgId());
        reminderRuleRepository.delete(rule);
    }
}
