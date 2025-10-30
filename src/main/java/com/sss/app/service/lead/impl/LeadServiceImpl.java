package com.sss.app.service.lead.impl;

import com.sss.app.dto.lead.LeadCreateRequestDTO;
import com.sss.app.dto.lead.LeadResponseDTO;
import com.sss.app.entity.lead.Lead;
import com.sss.app.helper.lead.LeadsHelper;
import com.sss.app.mapper.lead.LeadMapper;
import com.sss.app.repository.lead.LeadRepository;
import com.sss.app.service.lead.LeadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeadServiceImpl implements LeadService {

    private final LeadRepository leadRepository;
    private final LeadMapper leadMapper;
    private final LeadsHelper leadHelper;

    @Override
    public LeadResponseDTO createLead(LeadCreateRequestDTO payload) {
        return leadMapper.toResponse(leadHelper.createLead(payload));
    }
    @Override
    public LeadResponseDTO getLeadById(Long id) {
        return leadMapper.toResponse(leadHelper.getLeadById(id));
    }

    @Override
    public List<LeadResponseDTO> getAllLeads() {
        return leadRepository.findAll()
                .stream()
                .map(leadMapper::toResponse)
                .collect(Collectors.toList());
    }
}
