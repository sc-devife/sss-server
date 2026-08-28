package com.sss.app.controller;

import com.sss.app.dto.auth.UserSessionResponseDto;
import com.sss.app.entity.UserSession;
import com.sss.app.entity.users.User;
import com.sss.app.exception.NotFoundException;
import com.sss.app.jwtToken.JwtValidator;
import com.sss.app.repository.UserSessionRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

// Self-service "these are your active sessions, log out others" surface —
// no admin variant (viewing/revoking someone else's sessions) exists or is
// needed yet; every endpoint here is scoped to the caller's own sessions.
@RestController
@RequestMapping("/users/me/sessions")
@RequiredArgsConstructor
public class UserSessionsController {

    private final UserSessionRepository userSessionRepository;

    private User currentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private UUID currentSessionId(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String sessionIdStr = JwtValidator.extractSessionId(authHeader.substring("Bearer ".length()));
        if (!StringUtils.hasText(sessionIdStr)) {
            return null;
        }
        try {
            return UUID.fromString(sessionIdStr);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @GetMapping
    public ResponseEntity<List<UserSessionResponseDto>> listMySessions(HttpServletRequest request) {
        UUID currentSessionId = currentSessionId(request);
        List<UserSessionResponseDto> sessions = userSessionRepository
                .findAllByUser_SeqpAndRevokedAtIsNullOrderByLastAccessedDesc(currentUser().getSeqp())
                .stream()
                .map(s -> toDto(s, currentSessionId))
                .toList();
        return ResponseEntity.ok(sessions);
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Void> revokeSession(@PathVariable UUID sessionId) {
        UserSession session = userSessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("Session not found"));
        if (!session.getUser().getSeqp().equals(currentUser().getSeqp())) {
            throw new AccessDeniedException("You can only manage your own sessions");
        }
        session.setRevokedAt(LocalDateTime.now());
        userSessionRepository.save(session);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/revoke-others")
    public ResponseEntity<Void> revokeOtherSessions(HttpServletRequest request) {
        UUID currentSessionId = currentSessionId(request);
        if (currentSessionId == null) {
            throw new NotFoundException("Current session could not be identified");
        }
        userSessionRepository.revokeAllActiveForUserExcept(currentUser().getSeqp(), currentSessionId, LocalDateTime.now());
        return ResponseEntity.noContent().build();
    }

    private UserSessionResponseDto toDto(UserSession session, UUID currentSessionId) {
        return new UserSessionResponseDto(
                session.getSessionId(),
                session.getDeviceInfo(),
                session.getIpAddress(),
                session.getCreatedAt(),
                session.getLastAccessed(),
                session.getSessionId().equals(currentSessionId)
        );
    }
}
