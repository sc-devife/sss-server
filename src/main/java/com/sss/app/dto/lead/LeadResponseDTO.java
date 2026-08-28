package com.sss.app.dto.lead;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class LeadResponseDTO extends LeadDTO {
    private UUID uid;
    private LocalDate followUpDueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
