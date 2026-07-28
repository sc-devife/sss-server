package com.sss.app.helper.quote;

import com.sss.app.dto.quote.QuoteCreateRequestDTO;
import com.sss.app.dto.quote.QuoteUpdateRequestDTO;
import com.sss.app.entity.itinerary.Itinerary;
import com.sss.app.entity.quote.Quote;
import com.sss.app.entity.users.User;
import com.sss.app.exception.NotFoundException;
import com.sss.app.helper.itinerary.ItineraryHelper;
import com.sss.app.mapper.quote.QuoteMapper;
import com.sss.app.repository.quote.QuoteRepository;
import com.sss.app.security.OrgAccessGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class QuoteHelper {

    private final QuoteRepository quoteRepository;
    private final QuoteMapper quoteMapper;
    private final ItineraryHelper itineraryHelper;
    private final OrgAccessGuard orgAccessGuard;

    private User currentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public Quote create(QuoteCreateRequestDTO request) {
        Itinerary itinerary = itineraryHelper.getByUid(request.getItineraryUid());

        Quote quote = quoteMapper.toEntityCreate(request);
        quote.setOrgId(currentUser().getOrgId());
        quote.setItinerary(itinerary);

        return quoteRepository.save(quote);
    }

    public Quote getByUid(UUID uid) {
        Quote quote = quoteRepository.findByUid(uid)
                .orElseThrow(() -> new NotFoundException("Quote not found"));
        orgAccessGuard.requireAccessToOrg(quote.getOrgId());
        return quote;
    }

    public List<Quote> getAllForItinerary(UUID itineraryUid) {
        Itinerary itinerary = itineraryHelper.getByUid(itineraryUid);
        return quoteRepository.findAllByOrgIdAndItinerary_Seqp(currentUser().getOrgId(), itinerary.getSeqp());
    }

    public Quote update(UUID uid, QuoteUpdateRequestDTO request) {
        Quote quote = getByUid(uid);
        quoteMapper.updateEntityFromDto(request, quote);
        return quoteRepository.save(quote);
    }

    public void delete(UUID uid) {
        Quote quote = getByUid(uid);
        quoteRepository.delete(quote);
    }

    /** Same "new record, mark old superseded" pattern as Itinerary.createNewVersion. */
    public Quote createRevision(UUID sourceUid) {
        Quote source = getByUid(sourceUid);

        Quote revision = Quote.builder()
                .orgId(source.getOrgId())
                .itinerary(source.getItinerary())
                .version(source.getVersion() + 1)
                .status("draft")
                .currencyCode(source.getCurrencyCode())
                .fxRateSnapshot(source.getFxRateSnapshot())
                .subtotalUsd(source.getSubtotalUsd())
                .taxProfileId(source.getTaxProfileId())
                .taxAmountUsd(source.getTaxAmountUsd())
                .totalUsd(source.getTotalUsd())
                .discountType(source.getDiscountType())
                .discountValue(source.getDiscountValue())
                .templateId(source.getTemplateId())
                .validUntil(source.getValidUntil())
                .build();
        revision = quoteRepository.save(revision);

        source.setStatus("superseded");
        quoteRepository.save(source);

        return revision;
    }
}
