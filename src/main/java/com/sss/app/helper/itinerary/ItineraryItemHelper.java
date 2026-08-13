package com.sss.app.helper.itinerary;

import com.sss.app.dto.itinerary.ItineraryItemCreateRequestDTO;
import com.sss.app.dto.itinerary.ItineraryItemReorderRequestDTO;
import com.sss.app.dto.itinerary.ItineraryItemUpdateRequestDTO;
import com.sss.app.entity.itinerary.Itinerary;
import com.sss.app.entity.itinerary.ItineraryItem;
import com.sss.app.entity.users.User;
import com.sss.app.exception.BadRequestException;
import com.sss.app.exception.NotFoundException;
import com.sss.app.repository.itinerary.ItineraryItemRepository;
import com.sss.app.repository.library.activity.ActivityRepository;
import com.sss.app.repository.library.hotel.HotelRepository;
import com.sss.app.repository.library.serviceprovider.ServiceProviderRepository;
import com.sss.app.repository.library.transport.TransportRepository;
import com.sss.app.security.OrgAccessGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ItineraryItemHelper {

    private static final Set<String> VALID_TYPES = Set.of(
            "transport", "pickup_drop", "hotel", "activity", "sightseeing", "meal", "free_time", "other");

    // Which library table (if any) referenceId points into, per itemType.
    // transport/pickup_drop share Transport, activity/sightseeing share
    // Activity, meal/other point at ServiceProvider (restaurant/guide/misc
    // vendor); free_time never carries a reference.
    private enum RefKind { HOTEL, ACTIVITY, TRANSPORT, SERVICE_PROVIDER, NONE }

    private static final Map<String, RefKind> REF_KIND_BY_TYPE = Map.of(
            "transport", RefKind.TRANSPORT,
            "pickup_drop", RefKind.TRANSPORT,
            "hotel", RefKind.HOTEL,
            "activity", RefKind.ACTIVITY,
            "sightseeing", RefKind.ACTIVITY,
            "meal", RefKind.SERVICE_PROVIDER,
            "other", RefKind.SERVICE_PROVIDER,
            "free_time", RefKind.NONE);

    private final ItineraryItemRepository itineraryItemRepository;
    private final ItineraryHelper itineraryHelper;
    private final HotelRepository hotelRepository;
    private final ActivityRepository activityRepository;
    private final TransportRepository transportRepository;
    private final ServiceProviderRepository serviceProviderRepository;
    private final OrgAccessGuard orgAccessGuard;

    private User currentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public ItineraryItem create(ItineraryItemCreateRequestDTO request) {
        Itinerary itinerary = itineraryHelper.getByUid(request.getItineraryUid());
        validateReference(request.getItemType(), request.getReferenceId(), request.getTitle());

        ItineraryItem item = ItineraryItem.builder()
                .orgId(currentUser().getOrgId())
                .itinerary(itinerary)
                .dayNumber(request.getDayNumber())
                .itemType(request.getItemType())
                .referenceId(request.getReferenceId())
                .title(request.getTitle())
                .startTime(request.getStartTime())
                .notes(request.getNotes())
                .sortOrder(nextSortOrder(itinerary.getSeqp()))
                .build();

        return itineraryItemRepository.save(item);
    }

    public List<ItineraryItem> getAllForItinerary(UUID itineraryUid) {
        Itinerary itinerary = itineraryHelper.getByUid(itineraryUid);
        return itineraryItemRepository.findAllByItinerary_SeqpOrderByDayNumberAscSortOrderAsc(itinerary.getSeqp());
    }

    public ItineraryItem update(UUID uid, ItineraryItemUpdateRequestDTO request) {
        ItineraryItem item = getByUid(uid);

        if (request.getItemType() != null || request.getReferenceId() != null || request.getTitle() != null) {
            String newType = request.getItemType() != null ? request.getItemType() : item.getItemType();
            UUID newRefId = request.getReferenceId() != null ? request.getReferenceId() : item.getReferenceId();
            String newTitle = request.getTitle() != null ? request.getTitle() : item.getTitle();
            validateReference(newType, newRefId, newTitle);
            item.setItemType(newType);
            item.setReferenceId(newRefId);
            item.setTitle(newTitle);
            item.setSource(newRefId != null ? "library" : "custom");
        }
        if (request.getDayNumber() != null) {
            item.setDayNumber(request.getDayNumber());
        }
        if (request.getStartTime() != null) {
            item.setStartTime(request.getStartTime());
        }
        if (request.getNotes() != null) {
            item.setNotes(request.getNotes());
        }
        return itineraryItemRepository.save(item);
    }

    public void delete(UUID uid) {
        ItineraryItem item = getByUid(uid);
        itineraryItemRepository.delete(item);
    }

    public List<ItineraryItem> reorder(ItineraryItemReorderRequestDTO request) {
        Itinerary itinerary = itineraryHelper.getByUid(request.getItineraryUid());
        List<ItineraryItem> items = itineraryItemRepository.findAllByItinerary_SeqpOrderByDayNumberAscSortOrderAsc(itinerary.getSeqp());

        for (int i = 0; i < request.getOrderedItemUids().size(); i++) {
            UUID uid = request.getOrderedItemUids().get(i);
            int position = i;
            items.stream()
                    .filter(item -> item.getUid().equals(uid))
                    .findFirst()
                    .ifPresent(item -> item.setSortOrder(position));
        }

        return itineraryItemRepository.saveAll(items);
    }

    public String resolveLabel(ItineraryItem item) {
        if (item.getReferenceId() == null) {
            return item.getTitle();
        }
        RefKind kind = REF_KIND_BY_TYPE.get(item.getItemType());
        String resolved = kind == null ? null : switch (kind) {
            case HOTEL -> hotelRepository.findByUid(item.getReferenceId()).map(h -> h.getName()).orElse(null);
            case ACTIVITY -> activityRepository.findByUid(item.getReferenceId()).map(a -> a.getName()).orElse(null);
            case TRANSPORT -> transportRepository.findByUid(item.getReferenceId())
                    .map(t -> t.getModeCode() + (t.getVehicleTypeCode() != null ? " — " + t.getVehicleTypeCode() : ""))
                    .orElse(null);
            case SERVICE_PROVIDER -> serviceProviderRepository.findByUid(item.getReferenceId()).map(p -> p.getName()).orElse(null);
            case NONE -> null;
        };
        if (resolved != null) {
            return resolved;
        }
        return item.getTitle() != null ? item.getTitle() : "(deleted " + item.getItemType() + ")";
    }

    /**
     * Batch version of resolveLabel for a whole list — one query per RefKind
     * (4 total) instead of one query per item. Keyed by the item's own uid
     * (not referenceId) since the title-fallback is per-item and items with
     * no referenceId at all still need a key to store their label under.
     */
    public Map<UUID, String> resolveLabels(List<ItineraryItem> items) {
        List<ItineraryItem> withRef = items.stream().filter(i -> i.getReferenceId() != null).toList();

        List<UUID> hotelIds = byKind(withRef, RefKind.HOTEL);
        List<UUID> activityIds = byKind(withRef, RefKind.ACTIVITY);
        List<UUID> transportIds = byKind(withRef, RefKind.TRANSPORT);
        List<UUID> providerIds = byKind(withRef, RefKind.SERVICE_PROVIDER);

        Map<UUID, String> byReferenceId = new HashMap<>();
        if (!hotelIds.isEmpty()) {
            hotelRepository.findAllByUidIn(hotelIds).forEach(h -> byReferenceId.put(h.getUid(), h.getName()));
        }
        if (!activityIds.isEmpty()) {
            activityRepository.findAllByUidIn(activityIds).forEach(a -> byReferenceId.put(a.getUid(), a.getName()));
        }
        if (!transportIds.isEmpty()) {
            transportRepository.findAllByUidIn(transportIds).forEach(t ->
                    byReferenceId.put(t.getUid(), t.getModeCode() + (t.getVehicleTypeCode() != null ? " — " + t.getVehicleTypeCode() : "")));
        }
        if (!providerIds.isEmpty()) {
            serviceProviderRepository.findAllByUidIn(providerIds).forEach(p -> byReferenceId.put(p.getUid(), p.getName()));
        }

        Map<UUID, String> byItemUid = new HashMap<>();
        for (ItineraryItem item : items) {
            String fromLibrary = item.getReferenceId() != null ? byReferenceId.get(item.getReferenceId()) : null;
            String label = fromLibrary != null ? fromLibrary
                    : (item.getTitle() != null ? item.getTitle() : "(deleted " + item.getItemType() + ")");
            byItemUid.put(item.getUid(), label);
        }
        return byItemUid;
    }

    private List<UUID> byKind(List<ItineraryItem> items, RefKind kind) {
        return items.stream()
                .filter(i -> REF_KIND_BY_TYPE.get(i.getItemType()) == kind)
                .map(ItineraryItem::getReferenceId)
                .toList();
    }

    private void validateReference(String itemType, UUID referenceId, String title) {
        if (!VALID_TYPES.contains(itemType)) {
            throw new BadRequestException("itemType must be one of: " + VALID_TYPES);
        }
        // Activity items are always library-linked from the day planner's
        // "Add Activity" form (no free-text title field for that type) —
        // enforce it server-side too rather than trusting the client.
        if ("activity".equals(itemType) && referenceId == null) {
            throw new BadRequestException("referenceId is required for activity items");
        }
        boolean hasTitle = title != null && !title.isBlank();
        if (referenceId == null) {
            if (!hasTitle) {
                throw new BadRequestException("Either title or referenceId is required");
            }
            return;
        }
        RefKind kind = REF_KIND_BY_TYPE.get(itemType);
        boolean exists = switch (kind) {
            case HOTEL -> hotelRepository.findByUid(referenceId).isPresent();
            case ACTIVITY -> activityRepository.findByUid(referenceId).isPresent();
            case TRANSPORT -> transportRepository.findByUid(referenceId).isPresent();
            case SERVICE_PROVIDER -> serviceProviderRepository.findByUid(referenceId).isPresent();
            case NONE -> false;
        };
        if (!exists) {
            throw new NotFoundException("No " + itemType + " reference found with id: " + referenceId);
        }
    }

    private ItineraryItem getByUid(UUID uid) {
        ItineraryItem item = itineraryItemRepository.findByUid(uid)
                .orElseThrow(() -> new NotFoundException("Itinerary item not found"));
        orgAccessGuard.requireAccessToOrg(item.getOrgId());
        return item;
    }

    private int nextSortOrder(Long itinerarySeqp) {
        List<ItineraryItem> existing = itineraryItemRepository.findAllByItinerary_SeqpOrderByDayNumberAscSortOrderAsc(itinerarySeqp);
        return existing.stream().mapToInt(ItineraryItem::getSortOrder).max().orElse(-1) + 1;
    }
}
