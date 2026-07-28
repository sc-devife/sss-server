package com.sss.app.service.assignment.impl;

import com.sss.app.dto.assignment.PriorityCalendarEntryCreateRequestDTO;
import com.sss.app.dto.assignment.PriorityCalendarEntryResponseDTO;
import com.sss.app.helper.assignment.PriorityCalendarEntryHelper;
import com.sss.app.mapper.assignment.PriorityCalendarEntryMapper;
import com.sss.app.repository.assignment.PriorityCalendarEntryRepository;
import com.sss.app.service.assignment.PriorityCalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PriorityCalendarServiceImpl implements PriorityCalendarService {

    private final PriorityCalendarEntryHelper priorityCalendarEntryHelper;
    private final PriorityCalendarEntryMapper priorityCalendarEntryMapper;
    private final PriorityCalendarEntryRepository priorityCalendarEntryRepository;

    @Override
    public List<PriorityCalendarEntryResponseDTO> getAllForOrg() {
        return priorityCalendarEntryHelper.getAllForOrg().stream()
                .map(priorityCalendarEntryMapper::toResponse)
                .toList();
    }

    @Override
    public PriorityCalendarEntryResponseDTO create(PriorityCalendarEntryCreateRequestDTO request) {
        return priorityCalendarEntryMapper.toResponse(priorityCalendarEntryHelper.create(request));
    }

    @Override
    public void delete(UUID uid) {
        priorityCalendarEntryHelper.delete(uid);
    }

    @Override
    public boolean isDateInSeason(Long orgId, LocalDate date) {
        if (orgId == null || date == null) return false;
        return priorityCalendarEntryRepository.existsByOrgIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(orgId, date, date);
    }
}
