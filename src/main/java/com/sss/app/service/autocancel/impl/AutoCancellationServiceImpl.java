package com.sss.app.service.autocancel.impl;

import com.sss.app.entity.escape.Escape;
import com.sss.app.entity.escape.EscapeStatus;
import com.sss.app.entity.lead.Lead;
import com.sss.app.entity.lead.LeadStatus;
import com.sss.app.repository.escape.EscapeRepository;
import com.sss.app.repository.lead.LeadRepository;
import com.sss.app.service.autocancel.AutoCancellationService;
import com.sss.app.service.escape.EscapeLifecycleService;
import com.sss.app.service.lead.LeadLifecycleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AutoCancellationServiceImpl implements AutoCancellationService {

    private static final String ESCAPE_REASON =
            "Automatically cancelled because the date expired before confirmation/payment.";
    private static final String LEAD_REASON =
            "Automatically marked lost because the travel date expired before conversion.";

    // Every status strictly before "Escape Confirmed" — Planning through
    // Fully Paid. An escape reaching Escape Confirmed (or later) is never
    // auto-cancelled, even with an outstanding balance; Cancelled itself is
    // excluded too, so a re-run of the sweep never touches it again.
    private static final List<String> ESCAPE_EXPIRABLE_STATUSES = new ArrayList<>(
            EscapeStatus.ORDER.subList(0, EscapeStatus.indexOf(EscapeStatus.ESCAPE_CONFIRMED)));

    // Every non-terminal Lead status — a lead already Converted (or
    // Unqualified/Lost/Duplicate) is left alone.
    private static final List<String> LEAD_EXPIRABLE_STATUSES =
            List.of(LeadStatus.NEW, LeadStatus.CONTACTED, LeadStatus.QUALIFIED);

    private final EscapeRepository escapeRepository;
    private final LeadRepository leadRepository;
    private final EscapeLifecycleService escapeLifecycleService;
    private final LeadLifecycleService leadLifecycleService;

    // Once a day, at 8am server time — same cadence as the payment reminder
    // sweep (PaymentReminderServiceImpl), which this mirrors.
    @Scheduled(cron = "0 0 8 * * *")
    public void scheduledRun() {
        int escapes = autoCancelExpiredEscapes();
        int leads = autoCancelExpiredLeads();
        log.info("Auto-cancellation sweep: cancelled {} escape(s), marked {} lead(s) lost", escapes, leads);
    }

    @Override
    public int autoCancelExpiredEscapes() {
        LocalDate today = LocalDate.now();
        List<Escape> expired = escapeRepository.findAllByStatusInAndStartDateBefore(ESCAPE_EXPIRABLE_STATUSES, today);

        int count = 0;
        for (Escape escape : expired) {
            try {
                // Re-check under the lifecycle service's own guard (it also
                // rejects Cancelled/Ongoing/Completed) rather than trusting
                // the query snapshot alone — cheap insurance against a
                // status change that landed between the query and this call.
                escapeLifecycleService.cancel(escape.getUid(), ESCAPE_REASON);
                count++;
            } catch (Exception e) {
                log.error("Auto-cancellation sweep: failed to cancel escape {}", escape.getUid(), e);
            }
        }
        return count;
    }

    @Override
    public int autoCancelExpiredLeads() {
        LocalDate today = LocalDate.now();
        List<Lead> expired = leadRepository.findAllByStatusInAndTravelDateBefore(LEAD_EXPIRABLE_STATUSES, today);

        int count = 0;
        for (Lead lead : expired) {
            try {
                leadLifecycleService.markLost(lead.getUid(), LEAD_REASON);
                count++;
            } catch (Exception e) {
                log.error("Auto-cancellation sweep: failed to mark lead {} lost", lead.getUid(), e);
            }
        }
        return count;
    }
}
