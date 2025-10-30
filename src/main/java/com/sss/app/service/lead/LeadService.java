package com.sss.app.service.lead;

import com.sss.app.dto.lead.LeadCreateRequestDTO;
import com.sss.app.dto.lead.LeadResponseDTO;

import java.util.List;

public interface LeadService {
    LeadResponseDTO createLead(LeadCreateRequestDTO request);
    LeadResponseDTO getLeadById(Long id);
    List<LeadResponseDTO> getAllLeads();
}
