package com.sss.app.helper.deal;

import com.sss.app.entity.deal.Deal;
import com.sss.app.entity.escape.Escape;
import com.sss.app.entity.escape.EscapeStatus;
import com.sss.app.entity.itinerary.Itinerary;
import com.sss.app.entity.quote.Quote;
import com.sss.app.entity.users.User;
import com.sss.app.exception.ConflictException;
import com.sss.app.exception.NotFoundException;
import com.sss.app.helper.quote.QuoteHelper;
import com.sss.app.repository.deal.DealRepository;
import com.sss.app.repository.escape.EscapeRepository;
import com.sss.app.repository.itinerary.ItineraryRepository;
import com.sss.app.repository.quote.QuoteRepository;
import com.sss.app.security.OrgAccessGuard;
import com.sss.app.service.audit.AuditLogService;
import com.sss.app.service.escape.EscapeLifecycleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DealHelper {

    private static final Set<String> TERMINAL_QUOTE_STATUSES = Set.of("accepted", "rejected", "superseded");

    private final DealRepository dealRepository;
    private final QuoteHelper quoteHelper;
    private final QuoteRepository quoteRepository;
    private final ItineraryRepository itineraryRepository;
    private final EscapeRepository escapeRepository;
    private final EscapeLifecycleService escapeLifecycleService;
    private final AuditLogService auditLogService;
    private final OrgAccessGuard orgAccessGuard;

    private User currentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    /**
     * Section 8: "exactly one accepted quote (from one itinerary) per trip
     * converts to a Deal; all sibling itineraries/quotes remain as history,
     * marked superseded/rejected."
     */
    @Transactional
    public Deal acceptQuote(UUID quoteUid) {
        Quote quote = quoteHelper.getByUid(quoteUid);
        Itinerary winningItinerary = quote.getItinerary();
        Escape trip = winningItinerary.getEscape();

        if (TERMINAL_QUOTE_STATUSES.contains(quote.getStatus())) {
            throw new ConflictException("This quote is already " + quote.getStatus() + " and can't be accepted");
        }
        if (dealRepository.findByEscape_Seqp(trip.getSeqp()).isPresent()) {
            throw new ConflictException("This trip already has an accepted deal");
        }

        quote.setStatus("accepted");
        quoteRepository.save(quote);

        List<Itinerary> allItineraries = itineraryRepository.findAllByOrgIdAndEscape_Seqp(trip.getOrgId(), trip.getSeqp());
        for (Itinerary itinerary : allItineraries) {
            List<Quote> quotesInItinerary = quoteRepository.findAllByOrgIdAndItinerary_Seqp(trip.getOrgId(), itinerary.getSeqp());
            for (Quote sibling : quotesInItinerary) {
                if (!sibling.getSeqp().equals(quote.getSeqp()) && !TERMINAL_QUOTE_STATUSES.contains(sibling.getStatus())) {
                    sibling.setStatus("superseded");
                    quoteRepository.save(sibling);
                }
            }

            if (itinerary.getSeqp().equals(winningItinerary.getSeqp())) {
                itinerary.setStatus("active");
            } else if (!"superseded".equals(itinerary.getStatus())) {
                itinerary.setStatus("superseded");
            }
            itineraryRepository.save(itinerary);
        }

        Deal deal = Deal.builder()
                .orgId(trip.getOrgId())
                .escape(trip)
                .acceptedQuote(quote)
                .status("active")
                .build();
        deal = dealRepository.save(deal);

        auditLogService.record("Escape", trip.getSeqp(), "DEAL_CREATED", null, deal.getUid());

        // Best-effort: advance the trip's lifecycle if it hasn't already
        // passed this stage (e.g. re-accepting after a manual status jump).
        if (EscapeStatus.indexOf(trip.getStatus()) < EscapeStatus.indexOf(EscapeStatus.QUOTE_ACCEPTED)) {
            escapeLifecycleService.advance(trip.getUid(), EscapeStatus.QUOTE_ACCEPTED);
        }

        return deal;
    }

    public Deal getByUid(UUID uid) {
        Deal deal = dealRepository.findByUid(uid)
                .orElseThrow(() -> new NotFoundException("Deal not found"));
        orgAccessGuard.requireAccessToOrg(deal.getOrgId());
        return deal;
    }

    public Deal getForEscape(UUID escapeUid) {
        Escape trip = escapeRepository.findByUid(escapeUid)
                .orElseThrow(() -> new NotFoundException("Escape not found"));
        Deal deal = dealRepository.findByEscape_Seqp(trip.getSeqp())
                .orElseThrow(() -> new NotFoundException("No deal exists for this escape yet"));
        orgAccessGuard.requireAccessToOrg(deal.getOrgId());
        return deal;
    }
}
