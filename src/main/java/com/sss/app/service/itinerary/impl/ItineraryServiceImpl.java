package com.sss.app.service.itinerary.impl;

import com.sss.app.dto.itinerary.ItineraryCreateRequestDTO;
import com.sss.app.dto.itinerary.ItineraryResponseDTO;
import com.sss.app.dto.itinerary.ItineraryUpdateRequestDTO;
import com.sss.app.helper.itinerary.ItineraryHelper;
import com.sss.app.entity.itinerary.Itinerary;
import com.sss.app.entity.users.User;
import com.sss.app.mapper.itinerary.ItineraryMapper;
import com.sss.app.repository.UserRepository;
import com.sss.app.service.itinerary.ItineraryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ItineraryServiceImpl implements ItineraryService {

    private final ItineraryHelper itineraryHelper;
    private final ItineraryMapper itineraryMapper;
    private final UserRepository userRepository;

    @Override
    public ItineraryResponseDTO create(ItineraryCreateRequestDTO request) {
        return toResponse(itineraryHelper.create(request));
    }

    @Override
    public ItineraryResponseDTO getByUid(UUID uid) {
        return toResponse(itineraryHelper.getByUid(uid));
    }

    @Override
    public List<ItineraryResponseDTO> getAllForEscape(UUID escapeUid) {
        return itineraryHelper.getAllForEscape(escapeUid).stream().map(this::toResponse).toList();
    }

    @Override
    public ItineraryResponseDTO update(UUID uid, ItineraryUpdateRequestDTO request) {
        return toResponse(itineraryHelper.update(uid, request));
    }

    @Override
    public void delete(UUID uid) {
        itineraryHelper.delete(uid);
    }

    @Override
    public ItineraryResponseDTO duplicate(UUID sourceUid) {
        return toResponse(itineraryHelper.duplicate(sourceUid));
    }

    // MapStruct can't resolve createdBy (a user seqp) to a name declaratively.
    private ItineraryResponseDTO toResponse(Itinerary entity) {
        ItineraryResponseDTO dto = itineraryMapper.toResponse(entity);
        if (entity.getCreatedBy() != null) {
            userRepository.findById(entity.getCreatedBy()).map(User::getName).ifPresent(dto::setCreatedByName);
        }
        return dto;
    }
}
