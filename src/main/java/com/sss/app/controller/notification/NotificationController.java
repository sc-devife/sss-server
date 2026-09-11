package com.sss.app.controller.notification;

import com.sss.app.dto.notification.NotificationResponseDTO;
import com.sss.app.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    // Always scoped to the current authenticated user — no recipient is ever
    // accepted from the client, same convention as /follow-ups.
    @PreAuthorize("@permissionService.hasPermission('notifications.read')")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<NotificationResponseDTO>> getAll(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(notificationService.getAllForCurrentUser(pageable));
    }

    @PreAuthorize("@permissionService.hasPermission('notifications.read')")
    @GetMapping(value = "/unread-count", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Long>> unreadCount() {
        return ResponseEntity.ok(Map.of("count", notificationService.countUnreadForCurrentUser()));
    }

    @PreAuthorize("@permissionService.hasPermission('notifications.write')")
    @PatchMapping(value = "/{uid}/read", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<NotificationResponseDTO> markAsRead(@PathVariable UUID uid) {
        return ResponseEntity.ok(notificationService.markAsRead(uid));
    }

    @PreAuthorize("@permissionService.hasPermission('notifications.write')")
    @PatchMapping(value = "/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        notificationService.markAllAsRead();
        return ResponseEntity.noContent().build();
    }

    // The recipient is always the authenticated caller — same rule as
    // getAll()/unreadCount() above, and the reason a caller can never
    // subscribe to another user's or another org's stream: subscribe()
    // resolves the connection's owner from SecurityContextHolder, never
    // from anything the client sends.
    @PreAuthorize("@permissionService.hasPermission('notifications.read')")
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        return notificationService.subscribe();
    }
}
