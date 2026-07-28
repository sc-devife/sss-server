package com.sss.app.mapper.reminder;

import com.sss.app.dto.reminder.ReminderRuleResponseDTO;
import com.sss.app.entity.reminder.ReminderRule;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReminderRuleMapper {

    ReminderRuleResponseDTO toResponse(ReminderRule entity);
}
