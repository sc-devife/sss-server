package com.sss.app.repository.reminder;

import com.sss.app.entity.reminder.ReminderRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReminderRuleRepository extends JpaRepository<ReminderRule, Long> {

    Optional<ReminderRule> findByUid(UUID uid);

    List<ReminderRule> findAllByOrgId(Long orgId);

    List<ReminderRule> findAllByOrgIdAndIsActiveTrue(Long orgId);
}
