package com.sss.app.service.itinerary.impl;

import com.sss.app.dto.itinerary.ItineraryCreateRequestDTO;
import com.sss.app.dto.itinerary.ItineraryResponseDTO;
import com.sss.app.dto.itinerary.ItineraryUpdateRequestDTO;
import com.sss.app.helper.itinerary.ItineraryHelper;
import com.sss.app.mapper.itinerary.ItineraryMapper;
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

    @Override
    public ItineraryResponseDTO create(ItineraryCreateRequestDTO request) {
        return itineraryMapper.toResponse(itineraryHelper.create(request));
    }

    @Override
    public ItineraryResponseDTO getByUid(UUID uid) {
        return itineraryMapper.toResponse(itineraryHelper.getByUid(uid));
    }

    @Override
    public List<ItineraryResponseDTO> getAllForEscape(UUID escapeUid) {
        return itineraryHelper.getAllForEscape(escapeUid).stream().map(itineraryMapper::toResponse).toList();
    }

    @Override
    public ItineraryResponseDTO update(UUID uid, ItineraryUpdateRequestDTO request) {
        return itineraryMapper.toResponse(itineraryHelper.update(uid, request));
    }

    @Override
    public void delete(UUID uid) {
        itineraryHelper.delete(uid);
    }

    @Override
    public ItineraryResponseDTO duplicate(UUID sourceUid) {
        return itineraryMapper.toResponse(itineraryHelper.duplicate(sourceUid));
    }
}
