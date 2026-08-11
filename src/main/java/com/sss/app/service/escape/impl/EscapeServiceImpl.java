package com.sss.app.service.escape.impl;

import com.sss.app.dto.escape.EscapeCreateRequestDTO;
import com.sss.app.dto.escape.EscapeResponseDTO;
import com.sss.app.dto.escape.EscapeUpdateRequestDTO;
import com.sss.app.dto.lead.LeadResponseDTO;
import com.sss.app.dto.traveller.TravellerCreateRequestDTO;
import com.sss.app.helper.escape.EscapeHelper;
import com.sss.app.mapper.escape.EscapeMapper;
import com.sss.app.repository.UserRepository;
import com.sss.app.service.escape.EscapeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EscapeServiceImpl implements EscapeService {

    private final EscapeMapper escapeMapper;
    private final EscapeHelper escapeHelper;
    private final UserRepository userRepository;

    @Override
    public EscapeResponseDTO createEscape(EscapeCreateRequestDTO request) {

        return escapeMapper.toResponse(escapeHelper.createEscape(request));
    }

    @Override
    public EscapeResponseDTO updateEscape(UUID uid, EscapeUpdateRequestDTO request)
    {
        return escapeMapper.toResponse(escapeHelper.updateEscape(uid, request));
    }


    @Override
    public EscapeResponseDTO getEscapeById(UUID id) {
        EscapeResponseDTO response = escapeMapper.toResponse(escapeHelper.getEscapeById(id));
        enrichAssignedToName(response);
        return response;
    }

    @Override
    public List<EscapeResponseDTO> getAllEscapes() {
        return escapeHelper.getAllEscapes().stream()
                .map(escapeMapper::toResponse)
                .peek(this::enrichAssignedToName)
                .toList();
    }

    @Override
    public void deleteEscape(UUID uid) {
        escapeHelper.deleteEscape(uid);
    }

    @Override
    public Long resolveSeqp(UUID id) {
        return escapeHelper.getEscapeById(id).getSeqp();
    }

    @Override
    public EscapeResponseDTO addTraveller(UUID escapeUid, TravellerCreateRequestDTO request) {
        EscapeResponseDTO response = escapeMapper.toResponse(escapeHelper.addTraveller(escapeUid, request));
        enrichAssignedToName(response);
        return response;
    }

    @Override
    public void removeTraveller(UUID escapeUid, UUID travellerUid) {
        escapeHelper.removeTraveller(escapeUid, travellerUid);
    }

    // Joins the lead's assigned agent through the existing
    // Lead.assignedToUserId FK — not duplicated onto the leads table, just
    // looked up and attached to the response, same pattern as
    // UsersServiceImpl.toProfileResponseDto's organization join.
    private void enrichAssignedToName(EscapeResponseDTO response) {
        LeadResponseDTO lead = response.getLead();
        if (lead != null && lead.getAssignedToUserId() != null) {
            userRepository.findById(lead.getAssignedToUserId())
                    .ifPresent(user -> lead.setAssignedToUserName(user.getName()));
        }
    }

}
