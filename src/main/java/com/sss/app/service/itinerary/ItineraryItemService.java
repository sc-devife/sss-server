package com.sss.app.service.itinerary;

import com.sss.app.dto.itinerary.ItineraryItemCreateRequestDTO;
import com.sss.app.dto.itinerary.ItineraryItemReorderRequestDTO;
import com.sss.app.dto.itinerary.ItineraryItemResponseDTO;
import com.sss.app.dto.itinerary.ItineraryItemUpdateRequestDTO;

import java.util.List;
import java.util.UUID;

public interface ItineraryItemService {
    ItineraryItemResponseDTO create(ItineraryItemCreateRequestDTO request);
    List<ItineraryItemResponseDTO> getAllForItinerary(UUID itineraryUid);
    ItineraryItemResponseDTO update(UUID uid, ItineraryItemUpdateRequestDTO request);
    void delete(UUID uid);
    List<ItineraryItemResponseDTO> reorder(ItineraryItemReorderRequestDTO request);
}
