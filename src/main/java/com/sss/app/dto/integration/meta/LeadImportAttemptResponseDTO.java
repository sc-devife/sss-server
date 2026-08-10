package com.sss.app.dto.integration.meta;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class LeadImportAttemptResponseDTO {
    private UUID uid;
    private String provider;
    private String leadgenId;
    private String formId;
    private String pageId;
    private String adId;
    private String campaignId;
    private String status;
    private UUID leadUid;
    private String failureReason;
    private Integer retryCount;
    private LocalDateTime lastAttemptedAt;
    private LocalDateTime createdAt;
}
