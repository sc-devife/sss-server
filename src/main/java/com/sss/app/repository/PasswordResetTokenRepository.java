package com.sss.app.repository;

import com.sss.app.entity.auth.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByResetToken(String token);
    Optional<PasswordResetToken> findByEmail(String username);
}
