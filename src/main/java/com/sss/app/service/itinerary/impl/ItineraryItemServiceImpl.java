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
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ItineraryItemServiceImpl implements ItineraryItemService {

    private final ItineraryItemHelper itineraryItemHelper;

    @Override
    public ItineraryItemResponseDTO create(ItineraryItemCreateRequestDTO request) {
        ItineraryItem item = itineraryItemHelper.create(request);
        return toResponse(item, itineraryItemHelper.resolveLabel(item));
    }

    @Override
    public List<ItineraryItemResponseDTO> getAllForItinerary(UUID itineraryUid) {
        return toResponseList(itineraryItemHelper.getAllForItinerary(itineraryUid));
    }

    @Override
    public ItineraryItemResponseDTO update(UUID uid, ItineraryItemUpdateRequestDTO request) {
        ItineraryItem item = itineraryItemHelper.update(uid, request);
        return toResponse(item, itineraryItemHelper.resolveLabel(item));
    }

    @Override
    public void delete(UUID uid) {
        itineraryItemHelper.delete(uid);
    }

    @Override
    public List<ItineraryItemResponseDTO> reorder(ItineraryItemReorderRequestDTO request) {
        return toResponseList(itineraryItemHelper.reorder(request));
    }

    // Batch-resolves reference labels once per list (one query per RefKind)
    // instead of once per item — see ItineraryItemHelper.resolveLabels. Keyed
    // by item uid, not referenceId, since ad-hoc items (no referenceId) still
    // need a title-derived label.
    private List<ItineraryItemResponseDTO> toResponseList(List<ItineraryItem> items) {
        Map<UUID, String> labels = itineraryItemHelper.resolveLabels(items);
        return items.stream().map(item -> toResponse(item, labels.get(item.getUid()))).toList();
    }

    private ItineraryItemResponseDTO toResponse(ItineraryItem item, String referenceLabel) {
        ItineraryItemResponseDTO dto = new ItineraryItemResponseDTO();
        dto.setUid(item.getUid());
        dto.setItineraryUid(item.getItinerary().getUid());
        dto.setDayNumber(item.getDayNumber());
        dto.setItemType(item.getItemType());
        dto.setReferenceId(item.getReferenceId());
        dto.setReferenceLabel(referenceLabel);
        dto.setSource(item.getSource());
        dto.setTitle(item.getTitle());
        dto.setStartTime(item.getStartTime());
        dto.setNotes(item.getNotes());
        dto.setSortOrder(item.getSortOrder());
        return dto;
    }
}
