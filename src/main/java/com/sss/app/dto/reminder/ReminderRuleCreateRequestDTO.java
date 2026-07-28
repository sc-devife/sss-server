package com.sss.app.dto.reminder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReminderRuleCreateRequestDTO {

    @NotBlank(message = "Label is required")
    private String label;

    @NotNull(message = "offsetDays is required")
    private Integer offsetDays;

    private Boolean recurring;
}
