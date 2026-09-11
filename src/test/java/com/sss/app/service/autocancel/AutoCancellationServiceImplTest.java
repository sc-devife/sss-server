package com.sss.app.service.autocancel;

import com.sss.app.entity.escape.Escape;
import com.sss.app.entity.escape.EscapeStatus;
import com.sss.app.entity.lead.Lead;
import com.sss.app.entity.lead.LeadStatus;
import com.sss.app.exception.ConflictException;
import com.sss.app.repository.escape.EscapeRepository;
import com.sss.app.repository.lead.LeadRepository;
import com.sss.app.service.autocancel.impl.AutoCancellationServiceImpl;
import com.sss.app.service.escape.EscapeLifecycleService;
import com.sss.app.service.lead.LeadLifecycleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Exercises the sweep's own iterate/call/count logic (the repository's
 * derived-query filtering is Spring Data's own responsibility, not
 * meaningfully testable without a real database). Confirms: every escape
 * the repository hands back gets cancelled with the expected reason, every
 * lead gets marked lost the same way, a failure on one record doesn't stop
 * the rest of the sweep, and the eligible-status list passed to the
 * repository is Planning..Fully Paid — never Escape Confirmed or later,
 * and never Cancelled.
 */
@ExtendWith(MockitoExtension.class)
class AutoCancellationServiceImplTest {

    @Mock private EscapeRepository escapeRepository;
    @Mock private LeadRepository leadRepository;
    @Mock private EscapeLifecycleService escapeLifecycleService;
    @Mock private LeadLifecycleService leadLifecycleService;

    @InjectMocks
    private AutoCancellationServiceImpl autoCancellationService;

    @Captor private ArgumentCaptor<List<String>> statusListCaptor;

    private Escape escapeWith(String status) {
        return Escape.builder().seqp(1L).uid(UUID.randomUUID()).status(status)
                .startDate(LocalDate.now().minusDays(1)).build();
    }

    private Lead leadWith(String status) {
        return Lead.builder().seqp(1L).uid(UUID.randomUUID()).status(status)
                .travelDate(LocalDate.now().minusDays(1)).build();
    }

    @Test
    void autoCancelExpiredEscapes_cancelsEveryEscapeTheRepositoryReturns_withTheExpectedReason() {
        Escape planning = escapeWith(EscapeStatus.PLANNING);
        Escape paymentPending = escapeWith(EscapeStatus.PAYMENT_PENDING);
        when(escapeRepository.findAllByStatusInAndStartDateBefore(anyList(), any(LocalDate.class)))
                .thenReturn(List.of(planning, paymentPending));

        int count = autoCancellationService.autoCancelExpiredEscapes();

        assertThat(count).isEqualTo(2);
        verify(escapeLifecycleService).cancel(eq(planning.getUid()),
                eq("Automatically cancelled because the date expired before confirmation/payment."));
        verify(escapeLifecycleService).cancel(eq(paymentPending.getUid()),
                eq("Automatically cancelled because the date expired before confirmation/payment."));
    }

    @Test
    void autoCancelExpiredEscapes_neverIncludesEscapeConfirmedOrLaterOrCancelled_inTheEligibleStatusList() {
        when(escapeRepository.findAllByStatusInAndStartDateBefore(statusListCaptor.capture(), any(LocalDate.class)))
                .thenReturn(List.of());

        autoCancellationService.autoCancelExpiredEscapes();

        List<String> eligible = statusListCaptor.getValue();
        assertThat(eligible).containsExactly(
                EscapeStatus.PLANNING, EscapeStatus.ITINERARY_DRAFTING, EscapeStatus.QUOTATION_SENT,
                EscapeStatus.QUOTE_ACCEPTED, EscapeStatus.PAYMENT_PENDING, EscapeStatus.PARTIALLY_PAID,
                EscapeStatus.FULLY_PAID);
        assertThat(eligible).doesNotContain(EscapeStatus.ESCAPE_CONFIRMED, EscapeStatus.ONGOING,
                EscapeStatus.COMPLETED, EscapeStatus.CANCELLED);
    }

    @Test
    void autoCancelExpiredEscapes_aFailureOnOneRecord_doesNotStopTheRestOfTheSweep() {
        Escape first = escapeWith(EscapeStatus.PLANNING);
        Escape second = escapeWith(EscapeStatus.QUOTE_ACCEPTED);
        when(escapeRepository.findAllByStatusInAndStartDateBefore(anyList(), any(LocalDate.class)))
                .thenReturn(List.of(first, second));
        when(escapeLifecycleService.cancel(eq(first.getUid()), any()))
                .thenThrow(new ConflictException("already cancelled by someone else"));

        int count = autoCancellationService.autoCancelExpiredEscapes();

        assertThat(count).isEqualTo(1);
        verify(escapeLifecycleService).cancel(eq(first.getUid()), any());
        verify(escapeLifecycleService).cancel(eq(second.getUid()), any());
    }

    @Test
    void autoCancelExpiredLeads_marksEveryLeadTheRepositoryReturns_lost_withTheExpectedReason() {
        Lead lead = leadWith(LeadStatus.QUALIFIED);
        when(leadRepository.findAllByStatusInAndTravelDateBefore(anyList(), any(LocalDate.class)))
                .thenReturn(List.of(lead));

        int count = autoCancellationService.autoCancelExpiredLeads();

        assertThat(count).isEqualTo(1);
        verify(leadLifecycleService).markLost(eq(lead.getUid()),
                eq("Automatically marked lost because the travel date expired before conversion."));
    }

    @Test
    void autoCancelExpiredLeads_eligibleStatusListExcludesTerminalStatuses() {
        when(leadRepository.findAllByStatusInAndTravelDateBefore(statusListCaptor.capture(), any(LocalDate.class)))
                .thenReturn(List.of());

        autoCancellationService.autoCancelExpiredLeads();

        List<String> eligible = statusListCaptor.getValue();
        assertThat(eligible).containsExactly(LeadStatus.NEW, LeadStatus.CONTACTED, LeadStatus.QUALIFIED);
        assertThat(eligible).doesNotContainAnyElementsOf(LeadStatus.TERMINAL);
    }

    @Test
    void autoCancelExpiredLeads_aFailureOnOneRecord_doesNotStopTheRestOfTheSweep() {
        Lead first = leadWith(LeadStatus.NEW);
        Lead second = leadWith(LeadStatus.CONTACTED);
        when(leadRepository.findAllByStatusInAndTravelDateBefore(anyList(), any(LocalDate.class)))
                .thenReturn(List.of(first, second));
        when(leadLifecycleService.markLost(eq(first.getUid()), any()))
                .thenThrow(new ConflictException("already terminal"));

        int count = autoCancellationService.autoCancelExpiredLeads();

        assertThat(count).isEqualTo(1);
        verify(leadLifecycleService, times(1)).markLost(eq(second.getUid()), any());
    }
}
