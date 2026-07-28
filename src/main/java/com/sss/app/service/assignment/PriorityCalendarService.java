package com.sss.app.service.assignment;

import com.sss.app.dto.assignment.PriorityCalendarEntryCreateRequestDTO;
import com.sss.app.dto.assignment.PriorityCalendarEntryResponseDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface PriorityCalendarService {
    List<PriorityCalendarEntryResponseDTO> getAllForOrg();
    PriorityCalendarEntryResponseDTO create(PriorityCalendarEntryCreateRequestDTO request);
    void delete(UUID uid);

    /** Whether the given date falls inside any configured vacation-season window for the org. */
    boolean isDateInSeason(Long orgId, LocalDate date);
}
