package com.sss.app.dto.reminder;

import lombok.Data;

import java.util.UUID;

@Data
public class ReminderRuleResponseDTO {
    private UUID uid;
    private String label;
    private Integer offsetDays;
    private Boolean recurring;
    private Boolean isActive;
    private boolean fallback; // true when this row wasn't actually saved - it's the recommended default shown for orgs with no custom rules
}
