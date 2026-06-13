package com.sss.app.helper.library.escapesource;

import com.sss.app.dto.library.escapesource.EscapeSourceRequestDTO;
import com.sss.app.entity.library.escapesource.EscapeSource;
import com.sss.app.entity.library.escapesource.EscapeSourceB2BDetails;
import com.sss.app.exception.ConflictException;
import com.sss.app.exception.NotFoundException;
import com.sss.app.mapper.library.escapesource.EscapeSourceMapper;
import com.sss.app.repository.library.escapesource.EscapeSourceRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
//import jakarta.transaction.Transactional;
//import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

//import java.awt.print.Pageable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import java.util.List;

@Component
@RequiredArgsConstructor
public class EscapeSourceHelper {

    private final EscapeSourceRepository escapeSourceRepository;

    private final EscapeSourceMapper escapeSourceMapper;

    @PersistenceContext
    private final EntityManager entityManager;

    @Transactional
    public EscapeSource createEscapeSource(EscapeSourceRequestDTO payload) {

        // Check duplicate Full Name + Type
        if (escapeSourceRepository.existsByFullNameIgnoreCaseAndSourceType(payload.getFullName(), payload.getSourceType())) {
            throw new ConflictException(
                    "Escape Source already exists with name: "
                            + payload.getFullName()
                            + " and type: "
                            + payload.getSourceType()
            );
        }
        // Optional: Check Short Name uniqueness
        if (payload.getShortName() != null &&
                escapeSourceRepository.existsByShortNameIgnoreCase(payload.getShortName())) {

            throw new ConflictException(
                    "Escape Source already exists with short name: "
                            + payload.getShortName()
            );
        }

        EscapeSource newEscapeSource = escapeSourceMapper.toEntity(payload);

        //SET BACK-REFERENCE FOR FK
        if (newEscapeSource.getB2bDetails() != null) {
            newEscapeSource.getB2bDetails().setEscapeSource(newEscapeSource);
        }

        newEscapeSource = escapeSourceRepository.save(newEscapeSource);
        entityManager.refresh(newEscapeSource);
        return newEscapeSource;
    }
    @Transactional
    public EscapeSource updateEscapeSource(Long id, EscapeSourceRequestDTO payload) {

        EscapeSource existing =
                escapeSourceRepository.findById(id)
                        .orElseThrow(() -> new NotFoundException("Source not found: " + id));

        // ---------- Update Common Fields ----------
        existing.setFullName(payload.getFullName());
        existing.setShortName(payload.getShortName());
        existing.setTripSourceTag(payload.getTripSourceTag());
       // existing.setStatus(payload.getStatus());

        // ---------- Update B2B Details ----------
        if (payload.getB2bDetails() != null) {

            EscapeSourceB2BDetails details =
                    existing.getB2bDetails() != null
                            ? existing.getB2bDetails()
                            : new EscapeSourceB2BDetails();

            // map fields manually OR use mapper
            details.setContactName(payload.getB2bDetails().getContactName());
            details.setContactEmail(payload.getB2bDetails().getContactEmail());
            details.setContactPhone(payload.getB2bDetails().getContactPhone());

            details.setCity(payload.getB2bDetails().getCity());
            details.setState(payload.getB2bDetails().getState());
            details.setCountry(payload.getB2bDetails().getCountry());
            details.setPincode(payload.getB2bDetails().getPincode());
            details.setStreetAddress(payload.getB2bDetails().getStreetAddress());
            details.setLocality(payload.getB2bDetails().getLocality());
            details.setLandmark(payload.getB2bDetails().getLandmark());

            details.setBillingName(payload.getB2bDetails().getBillingName());
            details.setAdditionalBillingDetails(payload.getB2bDetails().getAdditionalBillingDetails());

            // IMPORTANT: maintain relationship
            details.setEscapeSource(existing);
            existing.setB2bDetails(details);
        }

        return escapeSourceRepository.save(existing);
    }
    @Transactional(readOnly = true)
    public EscapeSource getSourceById(Long id) {

        return escapeSourceRepository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException("Escape Source not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<EscapeSource> getAllSources() {
        return escapeSourceRepository.findAll();
    }
    @Transactional(readOnly = true)
    public Page<EscapeSource> getSources(Pageable pageable) {
        return escapeSourceRepository.findAll(pageable);
    }
}
