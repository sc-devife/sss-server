package com.sss.app.helper.library.inclusionexclusion;

import com.sss.app.dto.library.inclusionexclusion.InclusionExclusionCreateRequestDto;
import com.sss.app.dto.library.inclusionexclusion.InclusionExclusionUpdateRequestDto;
import com.sss.app.entity.library.inclusionexclusion.InclusionExclusion;
import com.sss.app.exception.ConflictException;
import com.sss.app.exception.NotFoundException;
import com.sss.app.mapper.library.inclusionexclusion.InclusionExclusionMapper;
import com.sss.app.repository.library.inclusionexlusion.InclusionExclusionRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class InclusionExclusionsHelper {

    private final InclusionExclusionRepository inclusionExclusionRepository;

    private final InclusionExclusionMapper inclusionExclusionMapper;

    @PersistenceContext
    private final EntityManager entityManager;

    public List<InclusionExclusion> fetchAllInclusionExclusions(Long companyId) {
        System.out.println("EscapePointsHelper fetchAllEscapePoints companyId === " + companyId);
        return inclusionExclusionRepository.findInclusionExclusionsByCompanyId(companyId);
    }

    public List<InclusionExclusion> fetchAllInclusions(Long companyId) {
        System.out.println("EscapePointsHelper fetchAllEscapePoints companyId === " + companyId);
        return inclusionExclusionRepository.findInclusionsByCompanyId(companyId);
    }

    public List<InclusionExclusion> fetchAllExclusions(Long companyId) {
        System.out.println("EscapePointsHelper fetchAllEscapePoints companyId === " + companyId);
        return inclusionExclusionRepository.findExclusionsByCompanyId(companyId);
    }

    public InclusionExclusion getInclusionExclusionByUid(String uid) {
        return inclusionExclusionRepository.findByUid(uid).orElseThrow(() -> new NotFoundException("Inclusion or Exclusion not found with uid: " + uid));
    }

    @Transactional
    public InclusionExclusion createInclusionExclusion(InclusionExclusionCreateRequestDto payload) {
        if (inclusionExclusionRepository.existsByName(payload.getName())) {
            throw new ConflictException("Inclusion or Exclusion already exists with Name: " + payload.getName());
        }

        InclusionExclusion newInclusionExclusion = inclusionExclusionMapper.toEntity(payload);
        newInclusionExclusion = inclusionExclusionRepository.save(newInclusionExclusion);
        entityManager.refresh(newInclusionExclusion);

        return newInclusionExclusion;
    }

    @Transactional
    public InclusionExclusion updateInclusionExclusion(String uid, InclusionExclusionUpdateRequestDto payload) {
        InclusionExclusion inclusionExclusion = getInclusionExclusionByUid(uid);
        System.out.println("updateInclusionExclusion inclusionExclusion - " + inclusionExclusion);
        System.out.println("updateInclusionExclusion payload - " + payload);
        inclusionExclusionMapper.updateFromDto(payload, inclusionExclusion);
        System.out.println("updateInclusionExclusion inclusionExclusion - " + inclusionExclusion);

        return inclusionExclusion;
    }
}
