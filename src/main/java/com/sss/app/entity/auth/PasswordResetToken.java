package com.sss.app.entity.auth;

import com.sss.app.util.IdGenerator;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_password_resets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetToken {
   /* @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;*/
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY) // or SEQUENCE if you're using a named sequence
   @Column(name = "seqp")
   private Long seqp;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID uid;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "reset_token", length = 255)
    private String resetToken;

    @Column(name = "token_expiry")
    private LocalDateTime tokenExpiry;

    @PrePersist
    protected void onCreate() {
        if (this.uid == null) {
            this.uid = IdGenerator.newUid();
        }
    }
}
