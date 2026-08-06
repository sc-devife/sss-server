package com.sss.app.service.itinerary;

import com.sss.app.dto.itinerary.ItineraryCreateRequestDTO;
import com.sss.app.dto.itinerary.ItineraryResponseDTO;
import com.sss.app.dto.itinerary.ItineraryUpdateRequestDTO;

import java.util.List;
import java.util.UUID;

public interface ItineraryService {
    ItineraryResponseDTO create(ItineraryCreateRequestDTO request);
    ItineraryResponseDTO getByUid(UUID uid);
    List<ItineraryResponseDTO> getAllForEscape(Long escapeId);
    ItineraryResponseDTO update(UUID uid, ItineraryUpdateRequestDTO request);
    void delete(UUID uid);
    ItineraryResponseDTO createNewVersion(UUID sourceUid);
}
