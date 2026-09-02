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
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DealHelper {

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
     * marked superseded/rejected." Enforced here, not just in the frontend,
     * so a stray API call (or old client) can't leave two quotes accepted at
     * once for the same escape.
     *
     * Accepting ANY non-rejected quote (draft/sent/superseded, or the
     * already-accepted one) is allowed at any time — it always supersedes
     * whichever quote was previously accepted and re-points the escape's one
     * Deal row (UNIQUE escape_id, V24 — created once, reused/reactivated
     * after that, never a second row) at the new quote. This is how
     * switching the accepted quote works, including un-doing a mistaken
     * acceptance by accepting a superseded one again — no separate
     * "cancel deal" step required.
     */
    @Transactional
    public Deal acceptQuote(UUID quoteUid) {
        Quote quote = quoteHelper.getByUid(quoteUid);
        Itinerary winningItinerary = quote.getItinerary();
        Escape trip = winningItinerary.getEscape();

        if ("rejected".equals(quote.getStatus())) {
            throw new ConflictException("This quote is already rejected and can't be accepted");
        }

        Deal existingDeal = dealRepository.findByEscape_Seqp(trip.getSeqp()).orElse(null);

        // Accepting the quote that is ALREADY this escape's active deal is a
        // harmless no-op re-run rather than genuinely new work — lets the
        // same "accept" action be safely retried, and doubles as a
        // self-healing path for any sibling quote left stuck "accepted" by
        // data older than this rule (re-running it drives the supersede
        // sweep below without changing which quote is actually accepted).
        boolean reaffirmingActiveDeal = existingDeal != null
                && "active".equals(existingDeal.getStatus())
                && existingDeal.getAcceptedQuote() != null
                && existingDeal.getAcceptedQuote().getSeqp().equals(quote.getSeqp());

        quote.setStatus("accepted");
        quoteRepository.save(quote);

        List<Itinerary> allItineraries = itineraryRepository.findAllByOrgIdAndEscape_Seqp(trip.getOrgId(), trip.getSeqp());
        for (Itinerary itinerary : allItineraries) {
            List<Quote> quotesInItinerary = quoteRepository.findAllByOrgIdAndItinerary_Seqp(trip.getOrgId(), itinerary.getSeqp());
            for (Quote sibling : quotesInItinerary) {
                // "accepted" is deliberately NOT treated as protected here —
                // the whole point of this sweep is to demote whichever quote
                // was previously accepted the moment a different one becomes
                // accepted, so at most one quote for this escape is ever
                // "accepted" at once.
                boolean protectedStatus = "rejected".equals(sibling.getStatus()) || "superseded".equals(sibling.getStatus());
                if (!sibling.getSeqp().equals(quote.getSeqp()) && !protectedStatus) {
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

        Deal deal;
        if (reaffirmingActiveDeal) {
            deal = existingDeal;
        } else if (existingDeal != null) {
            String previousStatus = existingDeal.getStatus();
            existingDeal.setAcceptedQuote(quote);
            existingDeal.setStatus("active");
            deal = dealRepository.save(existingDeal);
            auditLogService.record("Escape", trip.getSeqp(), "DEAL_QUOTE_SWITCHED", previousStatus, deal.getUid());
        } else {
            deal = Deal.builder()
                    .orgId(trip.getOrgId())
                    .escape(trip)
                    .acceptedQuote(quote)
                    .status("active")
                    .build();
            deal = dealRepository.save(deal);
            auditLogService.record("Escape", trip.getSeqp(), "DEAL_CREATED", null, deal.getUid());
        }

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

    /** Cancellation is recorded on the trip's own audit trail (mirrors Escape.cancel) rather than new Deal columns. */
    @Transactional
    public Deal cancel(UUID uid, String reason) {
        Deal deal = getByUid(uid);
        if ("cancelled".equals(deal.getStatus())) {
            throw new ConflictException("This deal is already cancelled");
        }
        deal.setStatus("cancelled");
        Deal saved = dealRepository.save(deal);
        auditLogService.record("Escape", deal.getEscape().getSeqp(), "DEAL_CANCELLED", "active", reason);
        return saved;
    }
}
