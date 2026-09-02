package com.sss.app.helper.deal;

import com.sss.app.entity.deal.Deal;
import com.sss.app.entity.escape.Escape;
import com.sss.app.entity.escape.EscapeStatus;
import com.sss.app.entity.itinerary.Itinerary;
import com.sss.app.entity.quote.Quote;
import com.sss.app.exception.ConflictException;
import com.sss.app.helper.quote.QuoteHelper;
import com.sss.app.repository.deal.DealRepository;
import com.sss.app.repository.escape.EscapeRepository;
import com.sss.app.repository.itinerary.ItineraryRepository;
import com.sss.app.repository.quote.QuoteRepository;
import com.sss.app.security.OrgAccessGuard;
import com.sss.app.service.audit.AuditLogService;
import com.sss.app.service.escape.EscapeLifecycleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * An Escape must end up with AT MOST one "accepted" quote at a time —
 * accepting a different quote has to demote whatever was previously
 * accepted to "superseded", including across the "cancel deal, then accept
 * a different quote" path (the deals table only ever holds one row per
 * escape — UNIQUE escape_id, V24 — so that path reactivates the existing
 * row rather than inserting a new one).
 */
@ExtendWith(MockitoExtension.class)
class DealHelperTest {

    @Mock private DealRepository dealRepository;
    @Mock private QuoteHelper quoteHelper;
    @Mock private QuoteRepository quoteRepository;
    @Mock private ItineraryRepository itineraryRepository;
    @Mock private EscapeRepository escapeRepository;
    @Mock private EscapeLifecycleService escapeLifecycleService;
    @Mock private AuditLogService auditLogService;
    @Mock private OrgAccessGuard orgAccessGuard;

    @InjectMocks
    private DealHelper dealHelper;

    private static final Long ORG_ID = 1L;

    private Escape escape;
    private Itinerary itinerary;
    private Quote quote1;
    private Quote quote2;

    @BeforeEach
    void setUp() {
        escape = Escape.builder()
                .seqp(100L)
                .uid(UUID.randomUUID())
                .orgId(ORG_ID)
                .status(EscapeStatus.QUOTE_ACCEPTED)
                .build();

        itinerary = Itinerary.builder()
                .seqp(200L)
                .uid(UUID.randomUUID())
                .escape(escape)
                .status("active")
                .build();

        quote1 = Quote.builder().seqp(300L).uid(UUID.randomUUID()).itinerary(itinerary).status("accepted").build();
        quote2 = Quote.builder().seqp(301L).uid(UUID.randomUUID()).itinerary(itinerary).status("draft").build();

        lenient().when(itineraryRepository.findAllByOrgIdAndEscape_Seqp(ORG_ID, escape.getSeqp()))
                .thenReturn(List.of(itinerary));
        lenient().when(itineraryRepository.save(any(Itinerary.class))).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(quoteRepository.save(any(Quote.class))).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(dealRepository.save(any(Deal.class))).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(quoteRepository.findAllByOrgIdAndItinerary_Seqp(ORG_ID, itinerary.getSeqp()))
                .thenReturn(List.of(quote1, quote2));
    }

    @Test
    void acceptingASecondQuote_supersedesThePreviouslyAcceptedOne() {
        // Quote 1 is already accepted; its deal was cancelled (the "Cancel
        // deal" -> "accept a different quote" flow) — quote2 is now accepted.
        Deal cancelledDeal = Deal.builder().uid(UUID.randomUUID()).orgId(ORG_ID).escape(escape)
                .acceptedQuote(quote1).status("cancelled").build();
        when(quoteHelper.getByUid(quote2.getUid())).thenReturn(quote2);
        when(dealRepository.findByEscape_Seqp(escape.getSeqp())).thenReturn(Optional.of(cancelledDeal));

        Deal result = dealHelper.acceptQuote(quote2.getUid());

        assertThat(quote1.getStatus()).isEqualTo("superseded");
        assertThat(quote2.getStatus()).isEqualTo("accepted");
        assertThat(result.getAcceptedQuote()).isEqualTo(quote2);
        assertThat(result.getStatus()).isEqualTo("active");
    }

    @Test
    void reacceptingTheCurrentlyAcceptedQuote_isANoOp() {
        // quote1 is already THE accepted quote behind an active deal —
        // accepting it again must not throw, must not touch the deal, and
        // (as a side effect) heals any sibling stuck "accepted" by data
        // older than this rule.
        Deal activeDeal = Deal.builder().uid(UUID.randomUUID()).orgId(ORG_ID).escape(escape)
                .acceptedQuote(quote1).status("active").build();
        quote2.setStatus("accepted"); // simulates a stale pre-existing bad state
        when(quoteHelper.getByUid(quote1.getUid())).thenReturn(quote1);
        when(dealRepository.findByEscape_Seqp(escape.getSeqp())).thenReturn(Optional.of(activeDeal));

        Deal result = dealHelper.acceptQuote(quote1.getUid());

        assertThat(quote1.getStatus()).isEqualTo("accepted");
        assertThat(quote2.getStatus()).isEqualTo("superseded");
        assertThat(result).isSameAs(activeDeal);
        assertThat(result.getAcceptedQuote()).isEqualTo(quote1);
    }

    @Test
    void acceptingADifferentQuote_whileADealIsStillActive_switchesTheDealToIt() {
        // No "cancel deal" step required — accepting a different (e.g.
        // superseded) quote while another is actively accepted just swaps
        // which quote the one Deal row points to.
        Deal activeDeal = Deal.builder().uid(UUID.randomUUID()).orgId(ORG_ID).escape(escape)
                .acceptedQuote(quote1).status("active").build();
        quote2.setStatus("superseded");
        when(quoteHelper.getByUid(quote2.getUid())).thenReturn(quote2);
        when(dealRepository.findByEscape_Seqp(escape.getSeqp())).thenReturn(Optional.of(activeDeal));

        Deal result = dealHelper.acceptQuote(quote2.getUid());

        assertThat(quote1.getStatus()).isEqualTo("superseded");
        assertThat(quote2.getStatus()).isEqualTo("accepted");
        assertThat(result).isSameAs(activeDeal);
        assertThat(result.getAcceptedQuote()).isEqualTo(quote2);
        assertThat(result.getStatus()).isEqualTo("active");
    }

    @Test
    void acceptingARejectedQuote_throwsConflict() {
        quote2.setStatus("rejected");
        when(quoteHelper.getByUid(quote2.getUid())).thenReturn(quote2);

        assertThatThrownBy(() -> dealHelper.acceptQuote(quote2.getUid()))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already rejected");
    }
}
