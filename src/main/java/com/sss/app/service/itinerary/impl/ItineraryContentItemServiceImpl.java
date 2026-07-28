package com.sss.app.service.itinerary.impl;

import com.sss.app.dto.itinerary.ItineraryContentItemAttachRequestDTO;
import com.sss.app.dto.itinerary.ItineraryContentItemCreateRequestDTO;
import com.sss.app.dto.itinerary.ItineraryContentItemResponseDTO;
import com.sss.app.dto.itinerary.ItineraryContentItemUpdateRequestDTO;
import com.sss.app.entity.itinerary.ItineraryContentItem;
import com.sss.app.helper.itinerary.ItineraryContentItemHelper;
import com.sss.app.service.itinerary.ItineraryContentItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ItineraryContentItemServiceImpl implements ItineraryContentItemService {

    private final ItineraryContentItemHelper itineraryContentItemHelper;

    @Override
    public ItineraryContentItemResponseDTO attach(ItineraryContentItemAttachRequestDTO request) {
        return toResponse(itineraryContentItemHelper.attach(request));
    }

    @Override
    public ItineraryContentItemResponseDTO create(ItineraryContentItemCreateRequestDTO request) {
        return toResponse(itineraryContentItemHelper.create(request));
    }

    @Override
    public List<ItineraryContentItemResponseDTO> getAllForItinerary(UUID itineraryUid) {
        return itineraryContentItemHelper.getAllForItinerary(itineraryUid).stream().map(this::toResponse).toList();
    }

    @Override
    public ItineraryContentItemResponseDTO update(UUID uid, ItineraryContentItemUpdateRequestDTO request) {
        return toResponse(itineraryContentItemHelper.update(uid, request));
    }

    @Override
    public void delete(UUID uid) {
        itineraryContentItemHelper.delete(uid);
    }

    private ItineraryContentItemResponseDTO toResponse(ItineraryContentItem item) {
        ItineraryContentItemResponseDTO dto = new ItineraryContentItemResponseDTO();
        dto.setUid(item.getUid());
        dto.setItineraryUid(item.getItinerary().getUid());
        dto.setType(item.getType());
        dto.setSourceItemId(item.getSourceItemId());
        dto.setName(item.getName());
        dto.setContentHtml(item.getContentHtml());
        dto.setSortOrder(item.getSortOrder());
        return dto;
    }
}
