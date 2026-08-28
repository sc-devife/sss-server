package com.sss.app.entity;

import com.sss.app.entity.users.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

// A real per-session UUID PK (replacing the old username-as-PK, which meant
// exactly one session could exist per user — a second device login silently
// invalidated the first). One user -> many sessions now, each independently
// revocable, with device/IP visibility for a "these are your active
// sessions" security feature.
@Entity
@Table(name = "user_sessions")
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserSession {

    @Id
    @Column(name = "session_id")
    private UUID sessionId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    @Column(name = "jwt_token", length = 2048, nullable = false)
    private String jwtToken;

    // Raw User-Agent string — good enough to distinguish "Chrome on Windows"
    // from "Safari on iPhone" in a session list without a full UA-parsing
    // dependency.
    @Column(name = "device_info")
    private String deviceInfo;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_accessed")
    private LocalDateTime lastAccessed;

    // Null = active. Soft-revoke (not delete) so a session list can still
    // show recently-ended sessions rather than just vanishing them.
    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    public static UserSession create(UUID sessionId, User user, String jwtToken, String deviceInfo, String ipAddress) {
        LocalDateTime now = LocalDateTime.now();
        return UserSession.builder()
                .sessionId(sessionId)
                .user(user)
                .jwtToken(jwtToken)
                .deviceInfo(deviceInfo)
                .ipAddress(ipAddress)
                .createdAt(now)
                .lastAccessed(now)
                .build();
    }

    public boolean isActive() {
        return revokedAt == null;
    }
}
