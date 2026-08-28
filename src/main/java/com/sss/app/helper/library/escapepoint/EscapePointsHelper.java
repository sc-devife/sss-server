package com.sss.app.helper.library.escapepoint;

import com.sss.app.dto.library.escapepoint.EscapePointCreateRequestDto;
import com.sss.app.dto.library.escapepoint.EscapePointLocationsUpdateRequestDto;
import com.sss.app.dto.library.escapepoint.EscapePointUpdateRequestDto;
import com.sss.app.entity.library.escapepoint.EscapePoint;
import com.sss.app.entity.library.escapepoint.EscapePointLocation;
import com.sss.app.entity.library.location.Location;
import com.sss.app.entity.users.User;
import com.sss.app.exception.ConflictException;
import com.sss.app.exception.NotFoundException;
import com.sss.app.mapper.library.escapepoint.EscapePointMapper;
import com.sss.app.repository.library.escapepoint.EscapePointLocationRepository;
import com.sss.app.repository.library.escapepoint.EscapePointRepository;
import com.sss.app.repository.library.location.LocationRepository;
import com.sss.app.security.OrgAccessGuard;
import com.sss.app.service.files.CloudinaryService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EscapePointsHelper {

    private final EscapePointRepository escapePointRepository;

    private final EscapePointMapper escapePointMapper;

    private final OrgAccessGuard orgAccessGuard;

    private final CloudinaryService cloudinaryService;

    private final EscapePointLocationRepository escapePointLocationRepository;

    private final LocationRepository locationRepository;

    @PersistenceContext
    private final EntityManager entityManager;

    private User currentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    /** Escape Points in the caller's own organization — Super Admins get their own org's view too, same as Users (Section 2). */
    public List<EscapePoint> fetchAllEscapePoints() {
        return escapePointRepository.findEscapePointsByOrgId(currentUser().getOrgId());
    }

    public EscapePoint getEscapePointByUid(String uid) {
        EscapePoint escapePoint = escapePointRepository.findByUid(uid)
                .orElseThrow(() -> new NotFoundException("Escape Point not found with uid: " + uid));
        orgAccessGuard.requireAccessToOrg(escapePoint.getOrgId());
        return escapePoint;
    }

    @Transactional
    public EscapePoint createEscapePoint(EscapePointCreateRequestDto payload) {
        if (escapePointRepository.existsById(payload.getId())) {
            throw new ConflictException("Escape Point already exists with id: " + payload.getId());
        }

        EscapePoint newEscapePoint = escapePointMapper.toEntity(payload);
        newEscapePoint.setOrgId(currentUser().getOrgId());
        newEscapePoint = escapePointRepository.save(newEscapePoint);
        entityManager.refresh(newEscapePoint);

        return newEscapePoint;
    }

    @Transactional
    public EscapePoint updateEscapePoint(String uid, EscapePointUpdateRequestDto payload) {
        EscapePoint escapePoint = getEscapePointByUid(uid);
        List<String> previousImages = escapePoint.getImages() == null ? null : new ArrayList<>(escapePoint.getImages());
        escapePointMapper.updateFromDto(payload, escapePoint);
        cloudinaryService.deleteRemoved(previousImages, escapePoint.getImages());

        return escapePoint;
    }

    @Transactional
    public void deleteEscapePoint(String uid) {
        EscapePoint escapePoint = getEscapePointByUid(uid);
        escapePoint.setDeletedAt(java.time.LocalDateTime.now());
        escapePoint.setStatus("archived");
    }

    // Which cities this destination covers, plus which one is the headline
    // display city — full-replace semantics (same "diff current vs desired"
    // shape as UsersHelper.reassignTeams), since the form always submits the
    // complete intended set rather than incremental add/remove.
    @Transactional
    public EscapePoint reassignLocations(String uid, EscapePointLocationsUpdateRequestDto payload) {
        EscapePoint escapePoint = getEscapePointByUid(uid);

        List<String> locationUidStrings = payload.getLocationUids() == null ? List.of() : payload.getLocationUids();
        List<UUID> locationUids = locationUidStrings.stream().map(UUID::fromString).toList();
        List<Location> locations = locationRepository.findAllByUidIn(locationUids);
        if (locations.size() != locationUids.size()) {
            throw new IllegalArgumentException("One or more locations not found");
        }

        UUID primaryUid = payload.getPrimaryLocationUid() == null || payload.getPrimaryLocationUid().isBlank()
                ? null : UUID.fromString(payload.getPrimaryLocationUid());
        if (primaryUid != null && locations.stream().noneMatch(l -> l.getUid().equals(primaryUid))) {
            throw new IllegalArgumentException("primaryLocationUid must be one of locationUids");
        }

        List<EscapePointLocation> current = escapePointLocationRepository.findAllByEscapePoint_Seqp(escapePoint.getSeqp());

        List<EscapePointLocation> toDelete = current.stream()
                .filter(link -> locations.stream().noneMatch(l -> l.getSeqp().equals(link.getLocation().getSeqp())))
                .toList();
        List<Location> toAdd = locations.stream()
                .filter(l -> current.stream().noneMatch(link -> link.getLocation().getSeqp().equals(l.getSeqp())))
                .toList();

        escapePointLocationRepository.deleteAll(toDelete);
        escapePointLocationRepository.saveAll(toAdd.stream()
                .map(l -> EscapePointLocation.create(escapePoint, l, l.getUid().equals(primaryUid)))
                .toList());

        // Re-flag isPrimary on the links that were kept (create() above only
        // covers newly-added ones).
        List<EscapePointLocation> kept = current.stream()
                .filter(link -> locations.stream().anyMatch(l -> l.getSeqp().equals(link.getLocation().getSeqp())))
                .toList();
        for (EscapePointLocation link : kept) {
            link.setIsPrimary(link.getLocation().getUid().equals(primaryUid));
        }
        escapePointLocationRepository.saveAll(kept);

        return escapePoint;
    }
}
