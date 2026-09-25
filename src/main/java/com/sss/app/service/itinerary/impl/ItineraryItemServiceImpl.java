package com.sss.app.service.itinerary.impl;

import com.sss.app.dto.email.SendEmailResponseDTO;
import com.sss.app.dto.itinerary.ItineraryItemCreateRequestDTO;
import com.sss.app.dto.itinerary.ItineraryItemReorderDaysRequestDTO;
import com.sss.app.dto.itinerary.ItineraryItemReorderRequestDTO;
import com.sss.app.dto.itinerary.ItineraryItemReplaceRequestDTO;
import com.sss.app.dto.itinerary.ItineraryItemResponseDTO;
import com.sss.app.dto.itinerary.ItineraryItemUpdateRequestDTO;
import com.sss.app.dto.library.transport.TransportCancellationEmailPreviewDTO;
import com.sss.app.entity.itinerary.ItineraryItem;
import com.sss.app.helper.itinerary.ItineraryItemHelper;
import com.sss.app.service.itinerary.ItineraryItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ItineraryItemServiceImpl implements ItineraryItemService {

    // referenceId points at a Transport library record for these two types —
    // see ItineraryItemHelper.REF_KIND_BY_TYPE. Only these two are worth a
    // transport-detail lookup per item.
    private static final Set<String> TRANSPORT_ITEM_TYPES = Set.of("transport", "pickup_drop");

    private static final Set<String> HOTEL_ITEM_TYPES = Set.of("hotel");

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

    @Override
    public List<ItineraryItemResponseDTO> reorderDays(ItineraryItemReorderDaysRequestDTO request) {
        return toResponseList(itineraryItemHelper.reorderDays(request));
    }

    @Override
    public ItineraryItemResponseDTO replaceHotel(UUID uid, ItineraryItemReplaceRequestDTO request) {
        ItineraryItem item = itineraryItemHelper.replaceHotel(uid, request);
        return toResponse(item, itineraryItemHelper.resolveLabel(item));
    }

    @Override
    public TransportCancellationEmailPreviewDTO getTransportCancellationEmailPreview(UUID uid) {
        return itineraryItemHelper.getTransportCancellationEmailPreview(uid);
    }

    @Override
    public SendEmailResponseDTO sendTransportCancellationEmail(UUID uid, String subject) {
        return itineraryItemHelper.sendTransportCancellationEmail(uid, subject);
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
        dto.setLongDescription(item.getLongDescription());
        dto.setPrice(item.getPrice());
        dto.setTravelersCount(item.getTravelersCount());
        dto.setSortOrder(item.getSortOrder());
        dto.setStatus(item.getStatus());
        dto.setDroppingReason(item.getDroppingReason());
        dto.setCancellationCharge(item.getCancellationChargeBase());
        dto.setReplacesItemUid(item.getReplacesItem() != null ? item.getReplacesItem().getUid() : null);
        if (TRANSPORT_ITEM_TYPES.contains(item.getItemType())) {
            dto.setTransportDetail(itineraryItemHelper.getTransportDetail(item));
        }
        if (HOTEL_ITEM_TYPES.contains(item.getItemType())) {
            dto.setHotelDetail(itineraryItemHelper.getHotelDetail(item));
        }
        return dto;
    }
}
