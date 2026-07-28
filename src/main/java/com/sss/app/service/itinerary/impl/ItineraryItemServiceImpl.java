package com.sss.app.service.itinerary.impl;

import com.sss.app.dto.itinerary.ItineraryItemCreateRequestDTO;
import com.sss.app.dto.itinerary.ItineraryItemReorderRequestDTO;
import com.sss.app.dto.itinerary.ItineraryItemResponseDTO;
import com.sss.app.dto.itinerary.ItineraryItemUpdateRequestDTO;
import com.sss.app.entity.itinerary.ItineraryItem;
import com.sss.app.helper.itinerary.ItineraryItemHelper;
import com.sss.app.service.itinerary.ItineraryItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ItineraryItemServiceImpl implements ItineraryItemService {

    private final ItineraryItemHelper itineraryItemHelper;

    @Override
    public ItineraryItemResponseDTO create(ItineraryItemCreateRequestDTO request) {
        return toResponse(itineraryItemHelper.create(request));
    }

    @Override
    public List<ItineraryItemResponseDTO> getAllForItinerary(UUID itineraryUid) {
        return itineraryItemHelper.getAllForItinerary(itineraryUid).stream().map(this::toResponse).toList();
    }

    @Override
    public ItineraryItemResponseDTO update(UUID uid, ItineraryItemUpdateRequestDTO request) {
        return toResponse(itineraryItemHelper.update(uid, request));
    }

    @Override
    public void delete(UUID uid) {
        itineraryItemHelper.delete(uid);
    }

    @Override
    public List<ItineraryItemResponseDTO> reorder(ItineraryItemReorderRequestDTO request) {
        return itineraryItemHelper.reorder(request).stream().map(this::toResponse).toList();
    }

    private ItineraryItemResponseDTO toResponse(ItineraryItem item) {
        ItineraryItemResponseDTO dto = new ItineraryItemResponseDTO();
        dto.setUid(item.getUid());
        dto.setItineraryUid(item.getItinerary().getUid());
        dto.setDayNumber(item.getDayNumber());
        dto.setItemType(item.getItemType());
        dto.setReferenceId(item.getReferenceId());
        dto.setReferenceLabel(itineraryItemHelper.resolveLabel(item.getItemType(), item.getReferenceId()));
        dto.setNotes(item.getNotes());
        dto.setSortOrder(item.getSortOrder());
        return dto;
    }
}
