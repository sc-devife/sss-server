package com.sss.app.dto.audit;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AuditLogResponseDTO {
    private String action;
    private Long performedBy;
    private String performedByName;
    private String previousValue;
    private String newValue;
    private LocalDateTime createdAt;
}
