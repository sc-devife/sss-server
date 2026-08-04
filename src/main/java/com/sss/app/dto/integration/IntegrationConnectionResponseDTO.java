package com.sss.app.dto.integration;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class IntegrationConnectionResponseDTO {
    private String channelCode;
    private String label;
    private boolean available; // false = "coming soon", not connectable yet
    private String status;     // connected / disconnected / error
    private Boolean autoCreateLeads;
    private LocalDateTime lastSyncedAt;

    // Meta (facebook/instagram) only — null for other channels. The access
    // token itself is never included here, in any form.
    private String platform;
    private String pageId;
    private String igAccountId;
    private String pageName;
    private LocalDateTime tokenLastVerifiedAt;
}
