package com.sss.app.helper.lead;

import com.sss.app.dto.lead.LeadCreateRequestDTO;
import com.sss.app.dto.lead.LeadResponseDTO;
import com.sss.app.entity.lead.Lead;
import com.sss.app.mapper.lead.LeadMapper;
import com.sss.app.repository.lead.LeadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class LeadsHelper {

    private final LeadRepository leadRepository;
    private final LeadMapper leadMapper;

    public Lead createLead(LeadCreateRequestDTO payload) {
        Lead lead = leadMapper.toEntity(payload);
        return leadRepository.save(lead);

    }

    public Lead getLeadById(Long id) {
        return leadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lead not found with id: " + id));
    }

    public List<Lead> getAllLeads() {
        return leadRepository.findAll();
    }

}
