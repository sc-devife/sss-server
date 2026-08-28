package com.sss.app.entity;

import com.sss.app.entity.users.User;
import com.sss.app.util.IdGenerator;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_credentials")
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
@Builder
public class UserCredential {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long seqp;

    // Real FK, replacing the old loose seqa/seqa_type polymorphic pair —
    // seqa_type was always the literal "users" in practice, so the loose
    // pattern bought nothing and cost a missing DB-level constraint.
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    @Column
    private UUID uid;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String password_hash;

    // Account-lockout / brute-force protection — no failed-login tracking
    // existed anywhere in the auth flow before this.
    @Builder.Default
    @Column(name = "failed_login_attempts", nullable = false)
    private Integer failedLoginAttempts = 0;

    // Null = not locked. Set once failedLoginAttempts crosses the threshold;
    // cleared on the next successful login or password reset.
    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    public static UserCredential create(User user, String password) {
        return UserCredential.builder()
                .user(user)
                .password_hash(password)
                .build();
    }

    public void update(String password) {
        this.setPassword_hash(password);
    }

    public boolean isLocked() {
        return lockedUntil != null && lockedUntil.isAfter(LocalDateTime.now());
    }

    public void registerFailedAttempt(int maxAttempts, Duration lockoutDuration) {
        int attempts = (this.failedLoginAttempts == null ? 0 : this.failedLoginAttempts) + 1;
        this.failedLoginAttempts = attempts;
        if (attempts >= maxAttempts) {
            this.lockedUntil = LocalDateTime.now().plus(lockoutDuration);
        }
    }

    public void registerSuccessfulLogin() {
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
    }

    @PrePersist
    protected void onCreate() {
        if (this.uid == null) {
            this.uid = IdGenerator.newUid();
        }
    }
}
