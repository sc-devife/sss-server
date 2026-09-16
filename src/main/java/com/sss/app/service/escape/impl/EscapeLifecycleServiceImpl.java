package com.sss.app.service.escape.impl;

import com.sss.app.dto.escape.EscapeResponseDTO;
import com.sss.app.entity.escape.Escape;
import com.sss.app.entity.escape.EscapeStatus;
import com.sss.app.entity.notification.NotificationType;
import com.sss.app.exception.BadRequestException;
import com.sss.app.exception.ConflictException;
import com.sss.app.helper.escape.EscapeHelper;
import com.sss.app.helper.itinerary.ItineraryItemHelper;
import com.sss.app.mapper.escape.EscapeMapper;
import com.sss.app.repository.escape.EscapeRepository;
import com.sss.app.service.audit.AuditLogService;
import com.sss.app.service.escape.EscapeLifecycleService;
import com.sss.app.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class EscapeLifecycleServiceImpl implements EscapeLifecycleService {

    private static final String ENTITY_TYPE = "Escape";

    // Section P0-2 — the dropping reason auto-applied to every still-active
    // Hotel/Activity/Transport booking cascaded by cancel(), below. Not the
    // Escape's own cancellation reason (that's a separate free-text the user
    // enters and is already recorded on the Escape's own audit entry) —
    // deliberately a fixed, generic string per the audit's own example.
    private static final String ESCAPE_CANCELLED_DROP_REASON = "Escape cancelled";

    // Suppressed here — the caller (DealHelper.acceptQuote / PaymentMilestoneHelper's
    // verify flow) already sends a more specific notification for these three
    // transitions; a generic "status changed" alongside it would be noise.
    private static final Set<String> SUPPRESS_GENERIC_STATUS_NOTIFICATION = Set.of(
            EscapeStatus.QUOTE_ACCEPTED, EscapeStatus.PARTIALLY_PAID, EscapeStatus.FULLY_PAID);

    private final EscapeHelper escapeHelper;
    private final EscapeRepository escapeRepository;
    private final EscapeMapper escapeMapper;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;
    private final ItineraryItemHelper itineraryItemHelper;

    @Override
    public EscapeResponseDTO advance(UUID escapeId, String targetStatus) {
        Escape escape = escapeHelper.getEscapeById(escapeId);

        int currentIndex = EscapeStatus.indexOf(escape.getStatus());
        int targetIndex = EscapeStatus.indexOf(targetStatus);

        if (targetIndex < 0) {
            throw new BadRequestException("Unknown escape status: " + targetStatus);
        }
        if (EscapeStatus.CANCELLED.equals(escape.getStatus())) {
            throw new ConflictException("This escape is cancelled and cannot move forward");
        }
        if (currentIndex < 0) {
            throw new ConflictException("Escape is in an unrecognized status: " + escape.getStatus());
        }
        if (targetIndex <= currentIndex) {
            throw new ConflictException("Cannot move from \"" + escape.getStatus() + "\" to \"" + targetStatus + "\" — status only moves forward");
        }

        String previousStatus = escape.getStatus();
        escape.setStatus(targetStatus);
        Escape saved = escapeRepository.save(escape);
        auditLogService.record(ENTITY_TYPE, escape.getSeqp(), "STATUS_ADVANCED", previousStatus, targetStatus);

        if (!SUPPRESS_GENERIC_STATUS_NOTIFICATION.contains(targetStatus)) {
            Long recipient = notificationService.resolveEscapeRecipient(saved);
            if (recipient != null) {
                notificationService.notify(recipient, saved.getOrgId(),
                        NotificationType.ESCAPE_STATUS_CHANGED, "Escape Status Changed",
                        "Escape " + safeTripCode(saved) + " moved from " + previousStatus + " to " + targetStatus + ".",
                        NotificationType.RelatedEntityType.ESCAPE, saved.getUid());
            } else {
                notificationService.notifyUsers(notificationService.resolveOrgManagers(saved.getOrgId()), saved.getOrgId(),
                        NotificationType.ESCAPE_STATUS_CHANGED, "Escape Status Changed",
                        "Escape " + safeTripCode(saved) + " moved from " + previousStatus + " to " + targetStatus + ".",
                        NotificationType.RelatedEntityType.ESCAPE, saved.getUid());
            }
        }

        return escapeMapper.toResponse(saved);
    }

    private String safeTripCode(Escape escape) {
        return escape.getTripCode() != null ? escape.getTripCode() : escape.getUid().toString();
    }

    @Override
    public EscapeResponseDTO cancel(UUID escapeId, String reason) {
        Escape escape = escapeHelper.getEscapeById(escapeId);

        int currentIndex = EscapeStatus.indexOf(escape.getStatus());
        int ongoingIndex = EscapeStatus.indexOf(EscapeStatus.ONGOING);
        if (EscapeStatus.CANCELLED.equals(escape.getStatus())) {
            throw new ConflictException("This escape is already cancelled");
        }
        if (currentIndex >= ongoingIndex) {
            throw new ConflictException("Escapes that are Ongoing or Completed can no longer be cancelled");
        }

        String previousStatus = escape.getStatus();
        escape.setStatus(EscapeStatus.CANCELLED);
        Escape saved = escapeRepository.save(escape);
        auditLogService.record(ENTITY_TYPE, escape.getSeqp(), "CANCELLED", previousStatus, reason);

        // Cascade (Section P0-2): any still-active (Initialize/Booked)
        // Hotel/Activity/Transport booking is dropped through the exact same
        // mechanism a user would trigger themselves — same audit trail, same
        // "left visible, never deleted" rule, same quote recompute. Runs in
        // this same @Transactional method, so a failure here rolls back the
        // status change above too, rather than leaving some bookings dropped
        // and the Escape only half-cancelled.
        itineraryItemHelper.dropActiveBookingsForEscape(saved, ESCAPE_CANCELLED_DROP_REASON);

        Long recipient = notificationService.resolveEscapeRecipient(saved);
        String message = "Escape " + safeTripCode(saved) + " has been cancelled.";
        if (recipient != null) {
            notificationService.notify(recipient, saved.getOrgId(), NotificationType.ESCAPE_CANCELLED,
                    "Escape Cancelled", message, NotificationType.RelatedEntityType.ESCAPE, saved.getUid());
        } else {
            notificationService.notifyUsers(notificationService.resolveOrgManagers(saved.getOrgId()), saved.getOrgId(),
                    NotificationType.ESCAPE_CANCELLED, "Escape Cancelled", message,
                    NotificationType.RelatedEntityType.ESCAPE, saved.getUid());
        }

        return escapeMapper.toResponse(saved);
    }

    // Idempotent: first call moves status -> Hold and audit-logs it exactly
    // like advance() does; a later call while already on Hold only updates
    // holdDate (no status write, no re-notification) — the Docs tab's own
    // "already on Hold, just let them change the date" flow relies on this.
    @Override
    public EscapeResponseDTO hold(UUID escapeId, LocalDate holdDate) {
        Escape escape = escapeHelper.getEscapeById(escapeId);

        if (EscapeStatus.CANCELLED.equals(escape.getStatus())) {
            throw new ConflictException("This escape is cancelled and cannot be put on hold");
        }
        if (EscapeStatus.COMPLETED.equals(escape.getStatus())) {
            throw new ConflictException("This escape is already completed and cannot be put on hold");
        }

        String previousStatus = escape.getStatus();
        boolean alreadyOnHold = EscapeStatus.HOLD.equals(previousStatus);

        escape.setStatus(EscapeStatus.HOLD);
        escape.setHoldDate(holdDate);
        Escape saved = escapeRepository.save(escape);

        auditLogService.record(ENTITY_TYPE, escape.getSeqp(),
                alreadyOnHold ? "HOLD_DATE_UPDATED" : "STATUS_ADVANCED",
                alreadyOnHold ? null : previousStatus,
                alreadyOnHold ? holdDate.toString() : EscapeStatus.HOLD);

        if (!alreadyOnHold) {
            Long recipient = notificationService.resolveEscapeRecipient(saved);
            String message = "Escape " + safeTripCode(saved) + " moved from " + previousStatus + " to " + EscapeStatus.HOLD + ".";
            if (recipient != null) {
                notificationService.notify(recipient, saved.getOrgId(), NotificationType.ESCAPE_STATUS_CHANGED,
                        "Escape Status Changed", message, NotificationType.RelatedEntityType.ESCAPE, saved.getUid());
            } else {
                notificationService.notifyUsers(notificationService.resolveOrgManagers(saved.getOrgId()), saved.getOrgId(),
                        NotificationType.ESCAPE_STATUS_CHANGED, "Escape Status Changed", message,
                        NotificationType.RelatedEntityType.ESCAPE, saved.getUid());
            }
        }

        return escapeMapper.toResponse(saved);
    }
}
