package com.sss.app.helper.library.escapepoint;

import com.sss.app.dto.library.escapepoint.EscapePointCreateRequestDto;
import com.sss.app.dto.library.escapepoint.EscapePointUpdateRequestDto;
import com.sss.app.entity.library.escapepoint.EscapePoint;
import com.sss.app.entity.users.User;
import com.sss.app.exception.ConflictException;
import com.sss.app.exception.NotFoundException;
import com.sss.app.mapper.library.escapepoint.EscapePointMapper;
import com.sss.app.repository.library.escapepoint.EscapePointRepository;
import com.sss.app.security.OrgAccessGuard;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class EscapePointsHelper {

    private final EscapePointRepository escapePointRepository;

    private final EscapePointMapper escapePointMapper;

    private final OrgAccessGuard orgAccessGuard;

    @PersistenceContext
    private final EntityManager entityManager;

    private User currentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    /** Destinations in the caller's own organization — Super Admins get their own org's view too, same as Users (Section 2). */
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
        escapePointMapper.updateFromDto(payload, escapePoint);

        return escapePoint;
    }

    @Transactional
    public void deleteEscapePoint(String uid) {
        EscapePoint escapePoint = getEscapePointByUid(uid);
        escapePoint.setDeletedAt(java.time.LocalDateTime.now());
        escapePoint.setStatus("archived");
    }
}
