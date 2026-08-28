package com.sss.app.jwtToken;

import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

@Component
public class JwtValidator {
    private static KeyProvider keyProvider = null;

    public JwtValidator(KeyProvider keyProvider) {
        JwtValidator.keyProvider = keyProvider;
    }

    public static String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(keyProvider.getPublicKey()).build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // Identifies exactly which UserSession row this token belongs to — needed
    // now that a user can hold multiple concurrent sessions (one per device),
    // so "is this token still valid" can no longer be answered by username
    // alone. Null for tokens issued before this claim existed (pre-migration
    // tokens) — callers must treat that as an invalid session, not skip the check.
    public static String extractSessionId(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(keyProvider.getPublicKey()).build()
                .parseClaimsJws(token)
                .getBody()
                .get("sessionId", String.class);
    }
}
