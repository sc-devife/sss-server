package com.sss.app.jwtToken;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Loads a persistent RSA keypair from JWT_PRIVATE_KEY/JWT_PUBLIC_KEY (base64-encoded
 * PKCS8/X509 DER) when configured. Without them, falls back to generating a fresh
 * in-memory keypair so local dev still works — but that means every issued token is
 * invalidated on restart and multiple instances won't accept each other's tokens, so
 * this fallback must not be relied on past local development.
 */
@Component
public class KeyProvider {

    private static final Logger log = LoggerFactory.getLogger(KeyProvider.class);

    @Value("${JWT_PRIVATE_KEY:}")
    private String configuredPrivateKey;

    @Value("${JWT_PUBLIC_KEY:}")
    private String configuredPublicKey;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    @PostConstruct
    public void init() throws Exception {
        if (!configuredPrivateKey.isBlank() && !configuredPublicKey.isBlank()) {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            this.privateKey = keyFactory.generatePrivate(
                    new PKCS8EncodedKeySpec(Base64.getDecoder().decode(configuredPrivateKey)));
            this.publicKey = keyFactory.generatePublic(
                    new X509EncodedKeySpec(Base64.getDecoder().decode(configuredPublicKey)));
            log.info("JWT signing key loaded from JWT_PRIVATE_KEY/JWT_PUBLIC_KEY — stable across restarts.");
        } else {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            this.privateKey = keyPair.getPrivate();
            this.publicKey = keyPair.getPublic();
            log.warn("No JWT_PRIVATE_KEY/JWT_PUBLIC_KEY configured — generated a throwaway RSA keypair for "
                    + "this process only. Every previously issued token will be rejected on the next restart, "
                    + "and multiple instances will not accept each other's tokens. Set JWT_PRIVATE_KEY and "
                    + "JWT_PUBLIC_KEY (base64-encoded PKCS8/X509 DER) before running this anywhere past local dev.");
        }
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }
}
