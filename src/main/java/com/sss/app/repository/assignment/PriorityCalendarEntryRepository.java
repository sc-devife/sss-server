package com.sss.app.repository.assignment;

import com.sss.app.entity.assignment.PriorityCalendarEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PriorityCalendarEntryRepository extends JpaRepository<PriorityCalendarEntry, Long> {

    Optional<PriorityCalendarEntry> findByUid(UUID uid);

    List<PriorityCalendarEntry> findAllByOrgId(Long orgId);

    boolean existsByOrgIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(Long orgId, LocalDate date1, LocalDate date2);
}
