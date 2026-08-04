package com.sss.app.service.lead;

import com.sss.app.dto.lead.LeadCreateRequestDTO;
import com.sss.app.dto.lead.LeadResponseDTO;
import com.sss.app.service.integration.ChannelLeadResult;
import com.sss.app.service.integration.NormalizedLeadPayload;
import com.sss.app.service.integration.ProviderLeadMetadata;

import java.util.List;

public interface LeadService {
    LeadResponseDTO createLead(LeadCreateRequestDTO request);
    LeadResponseDTO createLeadFromChannel(Long orgId, String channelCode, NormalizedLeadPayload payload);

    /** Lead Source Integration (Meta) path — adds dedup + provider metadata persistence. */
    ChannelLeadResult createLeadFromChannel(Long orgId, String channelCode, NormalizedLeadPayload payload, ProviderLeadMetadata sourceMetadata);

    LeadResponseDTO getLeadById(Long id);
    List<LeadResponseDTO> getAllLeads();
}
