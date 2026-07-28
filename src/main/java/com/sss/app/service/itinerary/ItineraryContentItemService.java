package com.sss.app.service.itinerary;

import com.sss.app.dto.itinerary.ItineraryContentItemAttachRequestDTO;
import com.sss.app.dto.itinerary.ItineraryContentItemCreateRequestDTO;
import com.sss.app.dto.itinerary.ItineraryContentItemResponseDTO;
import com.sss.app.dto.itinerary.ItineraryContentItemUpdateRequestDTO;

import java.util.List;
import java.util.UUID;

public interface ItineraryContentItemService {
    ItineraryContentItemResponseDTO attach(ItineraryContentItemAttachRequestDTO request);

    ItineraryContentItemResponseDTO create(ItineraryContentItemCreateRequestDTO request);

    List<ItineraryContentItemResponseDTO> getAllForItinerary(UUID itineraryUid);

    ItineraryContentItemResponseDTO update(UUID uid, ItineraryContentItemUpdateRequestDTO request);

    void delete(UUID uid);
}
