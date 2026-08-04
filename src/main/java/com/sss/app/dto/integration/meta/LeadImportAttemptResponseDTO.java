package com.sss.app.dto.integration.meta;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LeadImportAttemptResponseDTO {
    private Long seqp;
    private String provider;
    private String leadgenId;
    private String formId;
    private String pageId;
    private String adId;
    private String campaignId;
    private String status;
    private Long leadId;
    private String failureReason;
    private Integer retryCount;
    private LocalDateTime lastAttemptedAt;
    private LocalDateTime createdAt;
}
