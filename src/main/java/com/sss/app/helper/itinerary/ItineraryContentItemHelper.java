package com.sss.app.helper.itinerary;

import com.sss.app.dto.itinerary.ItineraryContentItemAttachRequestDTO;
import com.sss.app.dto.itinerary.ItineraryContentItemCreateRequestDTO;
import com.sss.app.dto.itinerary.ItineraryContentItemUpdateRequestDTO;
import com.sss.app.entity.itinerary.Itinerary;
import com.sss.app.entity.itinerary.ItineraryContentItem;
import com.sss.app.entity.library.inclusionexclusion.InclusionExclusion;
import com.sss.app.entity.library.inclusionexclusion.InclusionExclusionType;
import com.sss.app.entity.users.User;
import com.sss.app.exception.BadRequestException;
import com.sss.app.exception.NotFoundException;
import com.sss.app.repository.itinerary.ItineraryContentItemRepository;
import com.sss.app.repository.library.inclusionexlusion.InclusionExclusionRepository;
import com.sss.app.security.OrgAccessGuard;
import com.sss.app.util.RichTextSanitizer;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ItineraryContentItemHelper {

    private final ItineraryContentItemRepository itineraryContentItemRepository;
    private final InclusionExclusionRepository inclusionExclusionRepository;
    private final ItineraryHelper itineraryHelper;
    private final OrgAccessGuard orgAccessGuard;

    private User currentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public ItineraryContentItem attach(ItineraryContentItemAttachRequestDTO request) {
        Itinerary itinerary = itineraryHelper.getByUid(request.getItineraryUid());
        InclusionExclusion source = inclusionExclusionRepository.findByUid(request.getSourceItemUid())
                .orElseThrow(() -> new NotFoundException("Library item not found: " + request.getSourceItemUid()));
        orgAccessGuard.requireAccessToOrg(source.getOrgId());

        ItineraryContentItem item = ItineraryContentItem.builder()
                .orgId(currentUser().getOrgId())
                .itinerary(itinerary)
                .type(source.getType())
                .sourceItemId(source.getSeqp())
                .name(source.getName())
                .contentHtml(source.getContentHtml())
                .sortOrder(nextSortOrder(itinerary.getSeqp()))
                .build();

        return itineraryContentItemRepository.save(item);
    }

    public ItineraryContentItem create(ItineraryContentItemCreateRequestDTO request) {
        validateType(request.getType());
        Itinerary itinerary = itineraryHelper.getByUid(request.getItineraryUid());

        ItineraryContentItem item = ItineraryContentItem.builder()
                .orgId(currentUser().getOrgId())
                .itinerary(itinerary)
                .type(request.getType())
                .name(request.getName())
                .contentHtml(RichTextSanitizer.sanitize(request.getContentHtml()))
                .sortOrder(nextSortOrder(itinerary.getSeqp()))
                .build();

        return itineraryContentItemRepository.save(item);
    }

    public List<ItineraryContentItem> getAllForItinerary(UUID itineraryUid) {
        Itinerary itinerary = itineraryHelper.getByUid(itineraryUid);
        return itineraryContentItemRepository.findAllByItinerary_SeqpOrderByTypeAscSortOrderAsc(itinerary.getSeqp());
    }

    public ItineraryContentItem update(UUID uid, ItineraryContentItemUpdateRequestDTO request) {
        ItineraryContentItem item = getByUid(uid);
        if (request.getName() != null) {
            item.setName(request.getName());
        }
        if (request.getContentHtml() != null) {
            item.setContentHtml(RichTextSanitizer.sanitize(request.getContentHtml()));
        }
        return itineraryContentItemRepository.save(item);
    }

    public void delete(UUID uid) {
        itineraryContentItemRepository.delete(getByUid(uid));
    }

    private ItineraryContentItem getByUid(UUID uid) {
        ItineraryContentItem item = itineraryContentItemRepository.findByUid(uid)
                .orElseThrow(() -> new NotFoundException("Itinerary content item not found"));
        orgAccessGuard.requireAccessToOrg(item.getOrgId());
        return item;
    }

    private int nextSortOrder(Long itinerarySeqp) {
        return itineraryContentItemRepository.findAllByItinerary_SeqpOrderByTypeAscSortOrderAsc(itinerarySeqp)
                .stream().mapToInt(ItineraryContentItem::getSortOrder).max().orElse(-1) + 1;
    }

    private void validateType(String type) {
        if (!InclusionExclusionType.ALL.contains(type)) {
            throw new BadRequestException("type must be one of: " + InclusionExclusionType.ALL);
        }
    }
}
