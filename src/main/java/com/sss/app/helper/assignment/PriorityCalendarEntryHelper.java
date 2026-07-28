package com.sss.app.helper.assignment;

import com.sss.app.dto.assignment.PriorityCalendarEntryCreateRequestDTO;
import com.sss.app.entity.assignment.PriorityCalendarEntry;
import com.sss.app.entity.users.User;
import com.sss.app.exception.NotFoundException;
import com.sss.app.repository.assignment.PriorityCalendarEntryRepository;
import com.sss.app.security.OrgAccessGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PriorityCalendarEntryHelper {

    private final PriorityCalendarEntryRepository priorityCalendarEntryRepository;
    private final OrgAccessGuard orgAccessGuard;

    private User currentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public PriorityCalendarEntry create(PriorityCalendarEntryCreateRequestDTO request) {
        PriorityCalendarEntry entry = PriorityCalendarEntry.builder()
                .orgId(currentUser().getOrgId())
                .label(request.getLabel())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();
        return priorityCalendarEntryRepository.save(entry);
    }

    public List<PriorityCalendarEntry> getAllForOrg() {
        return priorityCalendarEntryRepository.findAllByOrgId(currentUser().getOrgId());
    }

    public void delete(UUID uid) {
        PriorityCalendarEntry entry = priorityCalendarEntryRepository.findByUid(uid)
                .orElseThrow(() -> new NotFoundException("Priority calendar entry not found"));
        orgAccessGuard.requireAccessToOrg(entry.getOrgId());
        priorityCalendarEntryRepository.delete(entry);
    }
}
