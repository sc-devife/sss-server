package com.sss.app.configuration;

import com.sss.app.entity.users.User;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Feeds @CreatedBy/@LastModifiedBy from the authenticated principal that
 * JwtAuthenticationFilter puts in the SecurityContext. Falls back to empty
 * for unauthenticated contexts (public endpoints, startup, tests).
 */
@Configuration
public class AuditorAwareConfig {

    @Bean
    public AuditorAware<Long> auditorAware() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()
                    || !(authentication.getPrincipal() instanceof User user)) {
                return Optional.empty();
            }
            return Optional.ofNullable(user.getSeqp());
        };
    }
}
