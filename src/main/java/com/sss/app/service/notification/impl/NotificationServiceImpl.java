package com.sss.app.service.notification.impl;

import com.sss.app.dto.notification.NotificationResponseDTO;
import com.sss.app.entity.escape.Escape;
import com.sss.app.entity.notification.Notification;
import com.sss.app.entity.users.User;
import com.sss.app.exception.NotFoundException;
import com.sss.app.repository.UserRepository;
import com.sss.app.repository.notification.NotificationRepository;
import com.sss.app.security.OrgAccessGuard;
import com.sss.app.service.notification.sse.SseEmitterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements com.sss.app.service.notification.NotificationService {

    private static final Set<String> MANAGER_ROLES = Set.of("SUPER_ADMIN", "ADMIN", "LEAD_ASSIGNER");

    // No server-side timeout — an open SSE connection is expected to live
    // for as long as the tab does. The heartbeat is what keeps a proxy/LB
    // from timing out an idle-looking connection, and a dead client is
    // detected by a failed send, not by this emitter expiring on its own.
    private static final long SSE_NO_TIMEOUT = 0L;

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final OrgAccessGuard orgAccessGuard;
    private final SseEmitterRegistry sseEmitterRegistry;

    // Nullable on purpose — some notify() call sites run on unauthenticated
    // paths (e.g. SignupHelper.createSignup, before login exists), where
    // there's no actor to compare against at all.
    private User currentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            return null;
        }
        return user;
    }

    @Override
    @Transactional
    public void notify(Long recipientUserSeqp, Long orgId, String type, String title, String message,
                        String relatedEntityType, UUID relatedEntityUid) {
        if (recipientUserSeqp == null) return;
        User actor = currentUser();
        if (actor != null && recipientUserSeqp.equals(actor.getSeqp())) return;

        User recipient = userRepository.findById(recipientUserSeqp).orElse(null);
        if (recipient == null) return;

        Notification notification = Notification.builder()
                .orgId(orgId)
                .recipientUser(recipient)
                .type(type)
                .title(title)
                .message(message)
                .relatedEntityType(relatedEntityType)
                .relatedEntityUid(relatedEntityUid)
                .build();
        notification = notificationRepository.save(notification);

        // Persistence comes first (above) and always succeeds or the
        // exception propagates normally — publishing to SSE is best-effort
        // delivery on top of an already-durable row. If the recipient has no
        // open connection (offline, or hasn't loaded the app yet) this is a
        // silent no-op; they'll see it via the normal notification API the
        // next time they load.
        sseEmitterRegistry.sendToUser(recipient.getSeqp(), "notification", toResponse(notification));
    }

    @Override
    @Transactional
    public void notifyUsers(List<Long> recipientUserSeqps, Long orgId, String type, String title, String message,
                             String relatedEntityType, UUID relatedEntityUid) {
        if (recipientUserSeqps == null || recipientUserSeqps.isEmpty()) return;
        recipientUserSeqps.stream().distinct()
                .forEach(seqp -> notify(seqp, orgId, type, title, message, relatedEntityType, relatedEntityUid));
    }

    @Override
    public List<Long> resolveOrgManagers(Long orgId) {
        return userRepository.findUsersWithRoles(orgId).stream()
                .filter(u -> u.getRoles().stream().anyMatch(link -> MANAGER_ROLES.contains(link.getRole().getName())))
                .map(User::getSeqp)
                .distinct()
                .toList();
    }

    @Override
    public Long resolveEscapeRecipient(Escape escape) {
        return escape != null ? escape.getAssignedToUserId() : null;
    }

    @Override
    public Page<NotificationResponseDTO> getAllForCurrentUser(Pageable pageable) {
        User user = currentUser();
        return notificationRepository
                .findAllByRecipientUser_SeqpAndOrgIdAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(
                        user.getSeqp(), user.getOrgId(), retentionCutoff(), pageable)
                .map(this::toResponse);
    }

    @Override
    public long countUnreadForCurrentUser() {
        User user = currentUser();
        return notificationRepository.countByRecipientUser_SeqpAndOrgIdAndIsReadFalseAndCreatedAtGreaterThanEqual(
                user.getSeqp(), user.getOrgId(), retentionCutoff());
    }

    private static LocalDateTime retentionCutoff() {
        return LocalDateTime.now().minusDays(Notification.RETENTION_DAYS);
    }

    @Override
    @Transactional
    public NotificationResponseDTO markAsRead(UUID uid) {
        Notification notification = notificationRepository.findByUid(uid)
                .orElseThrow(() -> new NotFoundException("Notification not found"));
        orgAccessGuard.requireAccessToOrg(notification.getOrgId());
        if (!notification.getRecipientUser().getSeqp().equals(currentUser().getSeqp())) {
            throw new NotFoundException("Notification not found");
        }
        if (notification.getCreatedAt().isBefore(retentionCutoff())) {
            throw new NotFoundException("Notification not found");
        }
        if (!Boolean.TRUE.equals(notification.getIsRead())) {
            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
        }
        return toResponse(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead() {
        User user = currentUser();
        notificationRepository.markAllAsRead(user.getSeqp(), user.getOrgId(), LocalDateTime.now());
    }

    @Override
    public SseEmitter subscribe() {
        User user = currentUser();
        SseEmitter emitter = new SseEmitter(SSE_NO_TIMEOUT);
        Long userSeqp = user.getSeqp();

        sseEmitterRegistry.register(userSeqp, emitter);
        emitter.onCompletion(() -> sseEmitterRegistry.remove(userSeqp, emitter));
        emitter.onTimeout(emitter::complete);
        emitter.onError(e -> sseEmitterRegistry.remove(userSeqp, emitter));

        // A first frame right away, before anything else is ever sent on this
        // connection — lets the client's EventSource fire onopen promptly and
        // gives the proxy chain an early flush instead of sitting on an empty
        // response until the first real notification (which may be minutes
        // or hours away).
        try {
            emitter.send(SseEmitter.event().name("connected").data(Map.of(), MediaType.APPLICATION_JSON));
        } catch (IOException e) {
            sseEmitterRegistry.remove(userSeqp, emitter);
        }

        return emitter;
    }

    private NotificationResponseDTO toResponse(Notification n) {
        NotificationResponseDTO dto = new NotificationResponseDTO();
        dto.setUid(n.getUid());
        dto.setType(n.getType());
        dto.setTitle(n.getTitle());
        dto.setMessage(n.getMessage());
        dto.setIsRead(n.getIsRead());
        dto.setCreatedAt(n.getCreatedAt());
        dto.setRelatedEntityType(n.getRelatedEntityType());
        dto.setRelatedEntityUid(n.getRelatedEntityUid());
        return dto;
    }
}
