package com.sss.app.dto.integration;

import lombok.Data;

@Data
public class IntegrationConnectRequestDTO {
    private String secret; // webhook shared secret; unused for future OAuth-based channels
    private Boolean autoCreateLeads;
}
