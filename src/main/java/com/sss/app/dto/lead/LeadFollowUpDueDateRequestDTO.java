package com.sss.app.dto.lead;

import lombok.Data;

import java.time.LocalDate;

@Data
public class LeadFollowUpDueDateRequestDTO {
    // Nullable on purpose — sending null clears a previously-set follow-up.
    private LocalDate followUpDueDate;
}
