package com.sss.app.helper.library.escapepoint;

import com.sss.app.dto.library.escapepoint.EscapePointCreateRequestDto;
import com.sss.app.dto.library.escapepoint.EscapePointUpdateRequestDto;
import com.sss.app.entity.escapepoint.EscapePoint;
import com.sss.app.exception.ConflictException;
import com.sss.app.exception.NotFoundException;
import com.sss.app.mapper.library.escapepoint.EscapePointMapper;
import com.sss.app.repository.library.escapepoint.EscapePointRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class EscapePointsHelper {

    private final EscapePointRepository escapePointRepository;

    private final EscapePointMapper escapePointMapper;

    @PersistenceContext
    private final EntityManager entityManager;

    public List<EscapePoint> fetchAllEscapePoints(Long companyId) {
        System.out.println("EscapePointsHelper fetchAllEscapePoints companyId === " + companyId);
        return escapePointRepository.findEscapePointsByCompanyId(companyId);
    }

    public EscapePoint getEscapePointByUid(String uid) {
        return escapePointRepository.findByUid(uid).orElseThrow(() -> new NotFoundException("Escape Point not found with uid: " + uid));
    }

    @Transactional
    public EscapePoint createEscapePoint(EscapePointCreateRequestDto payload) {
        if (escapePointRepository.existsById(payload.getId())) {
            throw new ConflictException("Escape Point already exists with id: " + payload.getId());
        }

        EscapePoint newEscapePoint = escapePointMapper.toEntity(payload);
        newEscapePoint = escapePointRepository.save(newEscapePoint);
        entityManager.refresh(newEscapePoint);

        return newEscapePoint;
    }

    @Transactional
    public EscapePoint updateEscapePoint(String uid, EscapePointUpdateRequestDto payload) {
        EscapePoint escapePoint = getEscapePointByUid(uid);
        System.out.println("updateEscapePoint escapePoint - " + escapePoint);
        System.out.println("updateEscapePoint payload - " + payload);
        escapePointMapper.updateFromDto(payload, escapePoint);
        System.out.println("updateEscapePoint escapePoint - " + escapePoint);

        return escapePoint;
    }
}
