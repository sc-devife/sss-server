package com.sss.app.service.autocancel;

/**
 * Daily housekeeping sweep: an Escape whose trip start date has passed while
 * it's still short of "Escape Confirmed" (whatever the reason — no quote
 * accepted yet, payment incomplete, or simply never actioned) is
 * automatically cancelled; a Lead whose travel date has passed while it's
 * still open (never converted, disqualified, lost or marked duplicate) is
 * automatically marked lost. Both reuse the existing lifecycle services
 * (EscapeLifecycleService.cancel / LeadLifecycleService.markLost), so the
 * status change, its audit-log entry, and its terminal-state guard are
 * identical to a human triggering the same action — this only decides *when*
 * to trigger it.
 */
public interface AutoCancellationService {
    /** Cancels every expired, not-yet-confirmed Escape. Returns how many were cancelled. */
    int autoCancelExpiredEscapes();

    /** Marks lost every expired, still-open Lead. Returns how many were marked lost. */
    int autoCancelExpiredLeads();
}
