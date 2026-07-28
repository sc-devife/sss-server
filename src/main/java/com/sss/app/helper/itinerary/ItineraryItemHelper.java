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
import com.sss.app.repository.library.transport.TransportRepository;
import com.sss.app.security.OrgAccessGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ItineraryItemHelper {

    private static final Set<String> VALID_TYPES = Set.of("hotel", "activity", "transport");

    private final ItineraryItemRepository itineraryItemRepository;
    private final ItineraryHelper itineraryHelper;
    private final HotelRepository hotelRepository;
    private final ActivityRepository activityRepository;
    private final TransportRepository transportRepository;
    private final OrgAccessGuard orgAccessGuard;

    private User currentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public ItineraryItem create(ItineraryItemCreateRequestDTO request) {
        Itinerary itinerary = itineraryHelper.getByUid(request.getItineraryUid());
        validateReference(request.getItemType(), request.getReferenceId());

        ItineraryItem item = ItineraryItem.builder()
                .orgId(currentUser().getOrgId())
                .itinerary(itinerary)
                .dayNumber(request.getDayNumber())
                .itemType(request.getItemType())
                .referenceId(request.getReferenceId())
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
        if (request.getDayNumber() != null) {
            item.setDayNumber(request.getDayNumber());
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

    public String resolveLabel(String itemType, UUID referenceId) {
        return switch (itemType) {
            case "hotel" -> hotelRepository.findByUid(referenceId).map(h -> h.getName()).orElse("(deleted hotel)");
            case "activity" -> activityRepository.findByUid(referenceId).map(a -> a.getName()).orElse("(deleted activity)");
            case "transport" -> transportRepository.findByUid(referenceId)
                    .map(t -> t.getModeCode() + (t.getVehicleTypeCode() != null ? " — " + t.getVehicleTypeCode() : ""))
                    .orElse("(deleted transport)");
            default -> "(unknown)";
        };
    }

    private void validateReference(String itemType, UUID referenceId) {
        if (!VALID_TYPES.contains(itemType)) {
            throw new BadRequestException("itemType must be one of: hotel, activity, transport");
        }
        boolean exists = switch (itemType) {
            case "hotel" -> hotelRepository.findByUid(referenceId).isPresent();
            case "activity" -> activityRepository.findByUid(referenceId).isPresent();
            case "transport" -> transportRepository.findByUid(referenceId).isPresent();
            default -> false;
        };
        if (!exists) {
            throw new NotFoundException("No " + itemType + " found with id: " + referenceId);
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
