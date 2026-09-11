package com.sss.app.service.notification;

import com.sss.app.dto.notification.NotificationResponseDTO;
import com.sss.app.entity.escape.Escape;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.UUID;

public interface NotificationService {

    /** No-ops if recipientUserSeqp is null or equals the current actor. */
    void notify(Long recipientUserSeqp, Long orgId, String type, String title, String message,
                String relatedEntityType, UUID relatedEntityUid);

    /** Bulk variant for audience-style notifications — de-duplicates, filters out the current actor. */
    void notifyUsers(List<Long> recipientUserSeqps, Long orgId, String type, String title, String message,
                      String relatedEntityType, UUID relatedEntityUid);

    /** SUPER_ADMIN/ADMIN/LEAD_ASSIGNER role holders in the org — the fallback audience for org-wide events. */
    List<Long> resolveOrgManagers(Long orgId);

    /** The escape's assignee, or null if unassigned. */
    Long resolveEscapeRecipient(Escape escape);

    Page<NotificationResponseDTO> getAllForCurrentUser(Pageable pageable);

    long countUnreadForCurrentUser();

    NotificationResponseDTO markAsRead(UUID uid);

    void markAllAsRead();

    /**
     * Opens a live SSE connection for the current user, registers it, and
     * returns it for the controller to hand back to the client. Delivery of
     * new notifications over this connection happens as a side effect of
     * {@link #notify}, not from here.
     */
    SseEmitter subscribe();
}
