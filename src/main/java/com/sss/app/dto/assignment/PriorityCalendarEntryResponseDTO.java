package com.sss.app.dto.assignment;

import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class PriorityCalendarEntryResponseDTO {
    private UUID uid;
    private String label;
    private LocalDate startDate;
    private LocalDate endDate;
}
