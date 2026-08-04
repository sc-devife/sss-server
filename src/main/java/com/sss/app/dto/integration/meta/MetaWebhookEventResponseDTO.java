package com.sss.app.dto.integration.meta;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MetaWebhookEventResponseDTO {
    private Long seqp;
    private String platform;
    private String pageId;
    private String leadgenId;
    private String formId;
    private String adId;
    private Boolean signatureValid;
    private String processingStatus;
    private String errorMessage;
    private String rawPayload;
    private LocalDateTime receivedAt;
}
