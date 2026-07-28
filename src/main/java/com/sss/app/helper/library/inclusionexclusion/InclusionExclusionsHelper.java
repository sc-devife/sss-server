package com.sss.app.helper.library.inclusionexclusion;

import com.sss.app.dto.library.inclusionexclusion.InclusionExclusionCreateRequestDto;
import com.sss.app.dto.library.inclusionexclusion.InclusionExclusionUpdateRequestDto;
import com.sss.app.entity.itinerary.Itinerary;
import com.sss.app.entity.library.escapepoint.EscapePoint;
import com.sss.app.entity.library.inclusionexclusion.InclusionExclusion;
import com.sss.app.entity.library.inclusionexclusion.InclusionExclusionType;
import com.sss.app.entity.users.User;
import com.sss.app.exception.BadRequestException;
import com.sss.app.exception.ConflictException;
import com.sss.app.exception.NotFoundException;
import com.sss.app.helper.itinerary.ItineraryHelper;
import com.sss.app.mapper.library.inclusionexclusion.InclusionExclusionMapper;
import com.sss.app.repository.library.escapepoint.EscapePointRepository;
import com.sss.app.repository.library.inclusionexlusion.InclusionExclusionRepository;
import com.sss.app.security.OrgAccessGuard;
import com.sss.app.util.RichTextSanitizer;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class InclusionExclusionsHelper {

    private final InclusionExclusionRepository inclusionExclusionRepository;
    private final EscapePointRepository escapePointRepository;
    private final ItineraryHelper itineraryHelper;
    private final OrgAccessGuard orgAccessGuard;

    private User currentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public List<InclusionExclusion> fetchAllForOrg(String type) {
        Long orgId = currentUser().getOrgId();
        return type != null
                ? inclusionExclusionRepository.findAllByOrgIdAndType(orgId, type)
                : inclusionExclusionRepository.findAllByOrgId(orgId);
    }

    public InclusionExclusion getByUid(String uid) {
        InclusionExclusion item = inclusionExclusionRepository.findByUid(uid)
                .orElseThrow(() -> new NotFoundException("Not found with uid: " + uid));
        orgAccessGuard.requireAccessToOrg(item.getOrgId());
        return item;
    }

    @Transactional
    public InclusionExclusion create(InclusionExclusionCreateRequestDto payload) {
        validateType(payload.getType());
        Long orgId = currentUser().getOrgId();
        if (inclusionExclusionRepository.existsByOrgIdAndTypeAndNameIgnoreCase(orgId, payload.getType(), payload.getName())) {
            throw new ConflictException("An item of this type already exists with name: " + payload.getName());
        }

        InclusionExclusion.InclusionExclusionBuilder builder = InclusionExclusion.builder()
                .orgId(orgId)
                .name(payload.getName())
                .type(payload.getType())
                .contentHtml(RichTextSanitizer.sanitize(payload.getContentHtml()))
                .sortOrder(payload.getSortOrder())
                .isActive(true);

        InclusionExclusion item = builder.build();
        if (payload.getDestinationId() != null && !payload.getDestinationId().isBlank()) {
            item.setDestination(resolveDestination(payload.getDestinationId()));
        }
        return inclusionExclusionRepository.save(item);
    }

    @Transactional
    public InclusionExclusion update(String uid, InclusionExclusionUpdateRequestDto payload) {
        validateType(payload.getType());
        InclusionExclusion item = getByUid(uid);
        item.setName(payload.getName());
        item.setType(payload.getType());
        item.setContentHtml(RichTextSanitizer.sanitize(payload.getContentHtml()));
        item.setSortOrder(payload.getSortOrder());
        if (payload.getDestinationId() != null && !payload.getDestinationId().isBlank()) {
            item.setDestination(resolveDestination(payload.getDestinationId()));
        } else {
            item.setDestination(null);
        }
        return inclusionExclusionRepository.save(item);
    }

    @Transactional
    public InclusionExclusion deactivate(String uid) {
        InclusionExclusion item = getByUid(uid);
        item.setIsActive(false);
        return inclusionExclusionRepository.save(item);
    }

    /** Items an itinerary builder can pick from: org-wide (unlinked) plus anything linked to the trip's own destinations. */
    public List<InclusionExclusion> getSelectableForItinerary(UUID itineraryUid, String type) {
        validateType(type);
        Itinerary itinerary = itineraryHelper.getByUid(itineraryUid);
        List<Long> destinationSeqps = itinerary.getEscape().getDestinations().stream()
                .map(EscapePoint::getSeqp)
                .toList();
        // JPQL "IN ()" on an empty collection is invalid — a sentinel that never
        // matches a real seqp keeps the query valid while still only returning
        // org-wide (unlinked) items when the trip has no destinations yet.
        List<Long> safeSeqps = destinationSeqps.isEmpty() ? List.of(-1L) : destinationSeqps;
        return inclusionExclusionRepository.findSelectableForDestinations(currentUser().getOrgId(), type, safeSeqps);
    }

    private EscapePoint resolveDestination(String destinationUid) {
        return escapePointRepository.findByUid(destinationUid)
                .orElseThrow(() -> new NotFoundException("Destination not found: " + destinationUid));
    }

    private void validateType(String type) {
        if (!InclusionExclusionType.ALL.contains(type)) {
            throw new BadRequestException("type must be one of: " + InclusionExclusionType.ALL);
        }
    }
}
