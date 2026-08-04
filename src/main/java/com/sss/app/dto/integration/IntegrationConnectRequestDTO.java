package com.sss.app.dto.integration;

import lombok.Data;

@Data
public class IntegrationConnectRequestDTO {
    private String secret; // webhook shared secret; unused for future OAuth-based channels
    private Boolean autoCreateLeads;

    // Meta (facebook/instagram) manual-token connect only — never echoed back
    // in any response DTO. pageId for facebook, igAccountId for instagram.
    private String accessToken;
    private String pageId;
    private String igAccountId;
    private String pageName;
}
