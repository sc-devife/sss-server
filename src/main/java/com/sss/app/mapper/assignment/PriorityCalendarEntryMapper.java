package com.sss.app.mapper.assignment;

import com.sss.app.dto.assignment.PriorityCalendarEntryResponseDTO;
import com.sss.app.entity.assignment.PriorityCalendarEntry;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PriorityCalendarEntryMapper {

    PriorityCalendarEntryResponseDTO toResponse(PriorityCalendarEntry entity);
}
