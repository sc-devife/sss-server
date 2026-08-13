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
        // getEscapeById already enforces the escape belongs to the caller's own org.
        Escape trip = escapeHelper.getEscapeById(request.getEscapeUid());
        String name = (request.getName() == null || request.getName().isBlank())
                ? buildAutoName(trip)
                : request.getName();

        Itinerary itinerary = Itinerary.builder()
                .orgId(currentUser().getOrgId())
                .escape(trip)
                .name(name)
                .status("draft")
                .version(1)
                .build();

        return itineraryRepository.save(itinerary);
    }

    // "<lead name> - Itinerary <next number> (<trip length> Days)" — mirrors
    // what an agent would otherwise have typed by hand when leaving the name
    // field blank.
    private String buildAutoName(Escape trip) {
        String escapeName = trip.getLead() != null && trip.getLead().getName() != null
                ? trip.getLead().getName()
                : "Escape #" + trip.getUid();
        long nextIndex = itineraryRepository.findAllByOrgIdAndEscape_Seqp(trip.getOrgId(), trip.getSeqp()).size() + 1;
        String daysPart = trip.getNumberOfDays() != null ? " (" + trip.getNumberOfDays() + " Days)" : "";
        return escapeName + " - Itinerary " + nextIndex + daysPart;
    }

    public Itinerary getByUid(UUID uid) {
        Itinerary itinerary = itineraryRepository.findByUid(uid)
                .orElseThrow(() -> new NotFoundException("Itinerary not found"));
        orgAccessGuard.requireAccessToOrg(itinerary.getOrgId());
        return itinerary;
    }

    public List<Itinerary> getAllForEscape(UUID escapeUid) {
        Escape trip = escapeHelper.getEscapeById(escapeUid); // org check
        return itineraryRepository.findAllByOrgIdAndEscape_Seqp(currentUser().getOrgId(), trip.getSeqp());
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
     * Itineraries are no longer versioned in place — "New version" is gone.
     * Duplicate creates a fully independent itinerary (new uid, own
     * day-plan/content clone) alongside the source; the source is left
     * untouched, not marked superseded.
     */
    @Transactional
    public Itinerary duplicate(UUID sourceUid) {
        Itinerary source = getByUid(sourceUid);
        List<ItineraryItem> sourceItems = itineraryItemRepository
                .findAllByItinerary_SeqpOrderByDayNumberAscSortOrderAsc(source.getSeqp());

        Itinerary copy = Itinerary.builder()
                .orgId(source.getOrgId())
                .escape(source.getEscape())
                .name(source.getName() + " (Copy)")
                .status("draft")
                .version(1)
                .build();
        copy = itineraryRepository.save(copy);

        Itinerary finalCopy = copy;
        List<ItineraryItem> clonedItems = sourceItems.stream()
                .map(item -> ItineraryItem.builder()
                        .orgId(item.getOrgId())
                        .itinerary(finalCopy)
                        .dayNumber(item.getDayNumber())
                        .itemType(item.getItemType())
                        .referenceId(item.getReferenceId())
                        .title(item.getTitle())
                        .startTime(item.getStartTime())
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
                        .itinerary(finalCopy)
                        .type(item.getType())
                        .sourceItemId(item.getSourceItemId())
                        .name(item.getName())
                        .contentHtml(item.getContentHtml())
                        .sortOrder(item.getSortOrder())
                        .build())
                .toList();
        itineraryContentItemRepository.saveAll(clonedContentItems);

        return copy;
    }
}
