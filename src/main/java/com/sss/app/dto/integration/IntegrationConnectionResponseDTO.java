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
}
