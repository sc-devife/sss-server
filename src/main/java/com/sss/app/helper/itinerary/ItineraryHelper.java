package com.sss.app.helper.itinerary;

import com.sss.app.dto.itinerary.ItineraryCreateRequestDTO;
import com.sss.app.dto.itinerary.ItineraryUpdateRequestDTO;
import com.sss.app.entity.escape.Escape;
import com.sss.app.entity.itinerary.Itinerary;
import com.sss.app.entity.itinerary.ItineraryContentItem;
import com.sss.app.entity.itinerary.ItineraryItem;
import com.sss.app.entity.users.User;
import com.sss.app.exception.NotFoundException;
import com.sss.app.helper.escape.EscapeHelper;
import com.sss.app.repository.itinerary.ItineraryContentItemRepository;
import com.sss.app.repository.itinerary.ItineraryItemRepository;
import com.sss.app.repository.itinerary.ItineraryRepository;
import com.sss.app.security.OrgAccessGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ItineraryHelper {

    private final ItineraryRepository itineraryRepository;
    private final ItineraryItemRepository itineraryItemRepository;
    private final ItineraryContentItemRepository itineraryContentItemRepository;
    private final EscapeHelper escapeHelper;
    private final OrgAccessGuard orgAccessGuard;

    private User currentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public Itinerary create(ItineraryCreateRequestDTO request) {
        // getEscapeById already enforces the trip belongs to the caller's own org.
        Escape trip = escapeHelper.getEscapeById(request.getTripId());

        Itinerary itinerary = Itinerary.builder()
                .orgId(currentUser().getOrgId())
                .escape(trip)
                .name(request.getName())
                .status("draft")
                .version(1)
                .build();

        return itineraryRepository.save(itinerary);
    }

    public Itinerary getByUid(UUID uid) {
        Itinerary itinerary = itineraryRepository.findByUid(uid)
                .orElseThrow(() -> new NotFoundException("Itinerary not found"));
        orgAccessGuard.requireAccessToOrg(itinerary.getOrgId());
        return itinerary;
    }

    public List<Itinerary> getAllForTrip(Long tripId) {
        escapeHelper.getEscapeById(tripId); // org check
        return itineraryRepository.findAllByOrgIdAndEscape_Seqp(currentUser().getOrgId(), tripId);
    }

    public Itinerary update(UUID uid, ItineraryUpdateRequestDTO request) {
        Itinerary itinerary = getByUid(uid);
        if (request.getName() != null) {
            itinerary.setName(request.getName());
        }
        if (request.getStatus() != null) {
            itinerary.setStatus(request.getStatus());
        }
        return itineraryRepository.save(itinerary);
    }

    public void delete(UUID uid) {
        Itinerary itinerary = getByUid(uid);
        itineraryRepository.delete(itinerary);
    }

    /**
     * Section 8: "keep every itinerary retained (versioned, not overwritten)"
     * — a revision is a new row + cloned items, not an edit to the original.
     * The source itinerary is marked superseded, never deleted.
     */
    @Transactional
    public Itinerary createNewVersion(UUID sourceUid) {
        Itinerary source = getByUid(sourceUid);
        List<ItineraryItem> sourceItems = itineraryItemRepository
                .findAllByItinerary_SeqpOrderByDayNumberAscSortOrderAsc(source.getSeqp());

        Itinerary newVersion = Itinerary.builder()
                .orgId(source.getOrgId())
                .escape(source.getEscape())
                .name(source.getName())
                .status("draft")
                .version(source.getVersion() + 1)
                .build();
        newVersion = itineraryRepository.save(newVersion);

        Itinerary finalNewVersion = newVersion;
        List<ItineraryItem> clonedItems = sourceItems.stream()
                .map(item -> ItineraryItem.builder()
                        .orgId(item.getOrgId())
                        .itinerary(finalNewVersion)
                        .dayNumber(item.getDayNumber())
                        .itemType(item.getItemType())
                        .referenceId(item.getReferenceId())
                        .notes(item.getNotes())
                        .sortOrder(item.getSortOrder())
                        .build())
                .toList();
        itineraryItemRepository.saveAll(clonedItems);

        List<ItineraryContentItem> sourceContentItems = itineraryContentItemRepository
                .findAllByItinerary_SeqpOrderByTypeAscSortOrderAsc(source.getSeqp());
        List<ItineraryContentItem> clonedContentItems = sourceContentItems.stream()
                .map(item -> ItineraryContentItem.builder()
                        .orgId(item.getOrgId())
                        .itinerary(finalNewVersion)
                        .type(item.getType())
                        .sourceItemId(item.getSourceItemId())
                        .name(item.getName())
                        .contentHtml(item.getContentHtml())
                        .sortOrder(item.getSortOrder())
                        .build())
                .toList();
        itineraryContentItemRepository.saveAll(clonedContentItems);

        source.setStatus("superseded");
        itineraryRepository.save(source);

        return newVersion;
    }
}
