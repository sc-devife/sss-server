package com.sss.app.service.escape.impl;

import com.sss.app.dto.escape.EscapeCreateRequestDTO;
import com.sss.app.dto.escape.EscapeResponseDTO;
import com.sss.app.dto.escape.EscapeUpdateRequestDTO;
import com.sss.app.dto.library.escapepoint.EscapePointResponseDto;
import com.sss.app.dto.traveller.TravellerCreateRequestDTO;
import com.sss.app.helper.escape.EscapeHelper;
import com.sss.app.mapper.escape.EscapeMapper;
import com.sss.app.repository.UserRepository;
import com.sss.app.service.escape.EscapeService;
import com.sss.app.service.library.escapepoint.EscapePointLocationResolver;
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
    private final EscapePointLocationResolver escapePointLocationResolver;

    @Override
    public EscapeResponseDTO createEscape(EscapeCreateRequestDTO request) {
        EscapeResponseDTO response = escapeMapper.toResponse(escapeHelper.createEscape(request));
        enrichEscapePointLocations(response);
        return response;
    }

    @Override
    public EscapeResponseDTO updateEscape(UUID uid, EscapeUpdateRequestDTO request)
    {
        EscapeResponseDTO response = escapeMapper.toResponse(escapeHelper.updateEscape(uid, request));
        enrichEscapePointLocations(response);
        return response;
    }


    @Override
    public EscapeResponseDTO getEscapeById(UUID id) {
        EscapeResponseDTO response = escapeMapper.toResponse(escapeHelper.getEscapeById(id));
        enrichAssignedToName(response);
        enrichEscapePointLocations(response);
        return response;
    }

    @Override
    public List<EscapeResponseDTO> getAllEscapes() {
        return escapeHelper.getAllEscapes().stream()
                .map(escapeMapper::toResponse)
                .peek(this::enrichAssignedToName)
                .peek(this::enrichEscapePointLocations)
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
        enrichEscapePointLocations(response);
        return response;
    }

    @Override
    public void removeTraveller(UUID escapeUid, UUID travellerUid) {
        escapeHelper.removeTraveller(escapeUid, travellerUid);
    }

    // Joins the escape's assigned agent through Escape.assignedToUserId —
    // not duplicated as a stored name, just looked up and attached to the
    // response, same pattern as UsersServiceImpl.toProfileResponseDto's
    // organization join.
    private void enrichAssignedToName(EscapeResponseDTO response) {
        if (response.getAssignedToUserId() != null) {
            userRepository.findById(response.getAssignedToUserId())
                    .ifPresent(user -> response.setAssignedToUserName(user.getName()));
        }
    }

    // Nested EscapePointResponseDto objects go through EscapePointMapper
    // directly (MapStruct's nested-object mapping), which deliberately
    // ignores locations/locationLabel (see EscapePointMapper) since that
    // resolution needs a repository lookup — done here instead, same
    // "resolved, not stored" pattern as enrichAssignedToName above.
    private void enrichEscapePointLocations(EscapeResponseDTO response) {
        List<EscapePointResponseDto> escapePoints = response.getEscapePoints();
        if (escapePoints == null || escapePoints.isEmpty()) {
            return;
        }
        escapePointLocationResolver.resolve(escapePoints.stream().map(EscapePointResponseDto::getSeqp).toList(), escapePoints);
    }

}
