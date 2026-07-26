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
}
