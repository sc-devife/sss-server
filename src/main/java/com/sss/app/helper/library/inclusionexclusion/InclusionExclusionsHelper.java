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
        if (payload.getEscapePointId() != null && !payload.getEscapePointId().isBlank()) {
            item.setEscapePoint(resolveEscapePoint(payload.getEscapePointId()));
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
        if (payload.getEscapePointId() != null && !payload.getEscapePointId().isBlank()) {
            item.setEscapePoint(resolveEscapePoint(payload.getEscapePointId()));
        } else {
            item.setEscapePoint(null);
        }
        return inclusionExclusionRepository.save(item);
    }

    @Transactional
    public InclusionExclusion deactivate(String uid) {
        InclusionExclusion item = getByUid(uid);
        item.setIsActive(false);
        return inclusionExclusionRepository.save(item);
    }

    /** Items an itinerary builder can pick from: org-wide (unlinked) plus anything linked to the escape's own escape points. */
    public List<InclusionExclusion> getSelectableForItinerary(UUID itineraryUid, String type) {
        validateType(type);
        Itinerary itinerary = itineraryHelper.getByUid(itineraryUid);
        List<Long> escapePointSeqps = itinerary.getEscape().getEscapePoints().stream()
                .map(EscapePoint::getSeqp)
                .toList();
        // JPQL "IN ()" on an empty collection is invalid — a sentinel that never
        // matches a real seqp keeps the query valid while still only returning
        // org-wide (unlinked) items when the escape has no escape points yet.
        List<Long> safeSeqps = escapePointSeqps.isEmpty() ? List.of(-1L) : escapePointSeqps;
        return inclusionExclusionRepository.findSelectableForEscapePoints(currentUser().getOrgId(), type, safeSeqps);
    }

    private EscapePoint resolveEscapePoint(String escapePointUid) {
        return escapePointRepository.findByUid(escapePointUid)
                .orElseThrow(() -> new NotFoundException("EscapePoint not found: " + escapePointUid));
    }

    private void validateType(String type) {
        if (!InclusionExclusionType.ALL.contains(type)) {
            throw new BadRequestException("type must be one of: " + InclusionExclusionType.ALL);
        }
    }
}
