package com.sss.app.service.integration;

import com.sss.app.dto.integration.IntegrationConnectRequestDTO;
import com.sss.app.dto.integration.IntegrationConnectionResponseDTO;

import java.util.List;

public interface IntegrationConnectionService {
    List<IntegrationConnectionResponseDTO> listForOrg();

    IntegrationConnectionResponseDTO connect(String channelCode, IntegrationConnectRequestDTO request);

    IntegrationConnectionResponseDTO disconnect(String channelCode);
}
