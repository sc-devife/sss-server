package com.sss.app.security.crypto;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Loads the AES-256 key used to encrypt Meta access tokens at rest from
 * META_TOKEN_ENCRYPTION_KEY (base64, must decode to exactly 32 bytes) —
 * mirrors jwtToken.KeyProvider's exact pattern for JWT_PRIVATE_KEY/PUBLIC_KEY.
 * Without it, falls back to a throwaway in-memory key so local dev still
 * works, but every previously stored token becomes undecryptable on restart.
 */
@Component
public class MetaTokenEncryptionKeyProvider {

    private static final Logger log = LoggerFactory.getLogger(MetaTokenEncryptionKeyProvider.class);

    @Value("${META_TOKEN_ENCRYPTION_KEY:}")
    private String configuredKey;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        if (!configuredKey.isBlank()) {
            byte[] decoded = Base64.getDecoder().decode(configuredKey);
            if (decoded.length != 32) {
                throw new IllegalStateException(
                        "META_TOKEN_ENCRYPTION_KEY must decode to 32 bytes (AES-256); got " + decoded.length);
            }
            this.secretKey = new SecretKeySpec(decoded, "AES");
            log.info("Meta token encryption key loaded from META_TOKEN_ENCRYPTION_KEY — stable across restarts.");
        } else {
            byte[] random = new byte[32];
            new SecureRandom().nextBytes(random);
            this.secretKey = new SecretKeySpec(random, "AES");
            log.warn("No META_TOKEN_ENCRYPTION_KEY configured — generated a throwaway key for this process "
                    + "only. Every previously stored Meta access token will fail to decrypt on the next "
                    + "restart. Set META_TOKEN_ENCRYPTION_KEY (base64, 32 bytes) before connecting any real "
                    + "Facebook/Instagram account.");
        }
    }

    public SecretKey getKey() {
        return secretKey;
    }
}
