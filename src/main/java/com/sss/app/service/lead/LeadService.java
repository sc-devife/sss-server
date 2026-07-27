package com.sss.app.service.lead;

import com.sss.app.dto.lead.LeadCreateRequestDTO;
import com.sss.app.dto.lead.LeadResponseDTO;
import com.sss.app.service.integration.NormalizedLeadPayload;

import java.util.List;

public interface LeadService {
    LeadResponseDTO createLead(LeadCreateRequestDTO request);
    LeadResponseDTO createLeadFromChannel(Long orgId, String channelCode, NormalizedLeadPayload payload);
    LeadResponseDTO getLeadById(Long id);
    List<LeadResponseDTO> getAllLeads();
}
