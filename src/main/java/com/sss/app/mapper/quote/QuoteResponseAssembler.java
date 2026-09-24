package com.sss.app.mapper.quote;

import com.sss.app.dto.quote.QuoteResponseDTO;
import com.sss.app.entity.quote.Quote;
import com.sss.app.entity.users.User;
import com.sss.app.repository.UserRepository;
import com.sss.app.service.quote.QuoteFingerprintService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Wraps QuoteMapper#toResponse to also resolve createdBy (a user seqp) into
 * createdByName — MapStruct can't do that lookup declaratively, and every
 * place that turns a Quote into a QuoteResponseDTO (QuoteServiceImpl,
 * QuoteComputationServiceImpl) needs it, so it lives here once instead of
 * being duplicated (or, worse, silently skipped — a Quote response built
 * from the bare QuoteMapper always has a null createdByName, which the
 * frontend renders as "System").
 */
@Component
@RequiredArgsConstructor
public class QuoteResponseAssembler {

    private final QuoteMapper quoteMapper;
    private final UserRepository userRepository;
    private final QuoteFingerprintService quoteFingerprintService;

    public QuoteResponseDTO toResponse(Quote entity) {
        QuoteResponseDTO dto = quoteMapper.toResponse(entity);
        if (entity.getCreatedBy() != null) {
            userRepository.findById(entity.getCreatedBy()).map(User::getName).ifPresent(dto::setCreatedByName);
        }
        dto.setChangedSinceGenerated(entity.getGeneratedAt() != null
                && !quoteFingerprintService.compute(entity).equals(entity.getGeneratedFingerprint()));
        return dto;
    }
}
