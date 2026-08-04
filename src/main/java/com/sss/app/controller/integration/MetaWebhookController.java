package com.sss.app.controller.integration;

import com.sss.app.security.crypto.MetaSignatureVerifier;
import com.sss.app.service.integration.meta.MetaWebhookOrchestrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Receives Meta's Lead Ads webhook (Facebook Page + Instagram Business
 * Account leadgen events share this one callback URL — Meta distinguishes
 * them via the payload's top-level "object" field, not the URL). Deliberately
 * public/unauthenticated (see JwtAuthenticationFilter.PUBLIC_PATHS and
 * SecurityConfig) — secured instead by real HMAC signature verification,
 * unlike the older generic /api/integrations/webhook/ channel's shared-secret
 * header check.
 */
@RestController
@RequestMapping("/api/integrations/meta/webhook")
@RequiredArgsConstructor
public class MetaWebhookController {

    @Value("${META_WEBHOOK_VERIFY_TOKEN:}")
    private String verifyToken;

    private final MetaWebhookOrchestrationService orchestrationService;
    private final MetaSignatureVerifier signatureVerifier;

    /** Meta's one-time subscription handshake when registering this callback URL in the App Dashboard. */
    @GetMapping
    public ResponseEntity<String> verifyChallenge(
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String token,
            @RequestParam("hub.challenge") String challenge) {
        if (!"subscribe".equals(mode) || verifyToken.isBlank() || !verifyToken.equals(token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(challenge);
    }

    @PostMapping
    public ResponseEntity<Void> receive(
            @RequestBody String rawBody,
            @RequestHeader(value = "X-Hub-Signature-256", required = false) String signatureHeader) {
        boolean valid = signatureVerifier.isValid(rawBody, signatureHeader);
        orchestrationService.handleWebhookDelivery(rawBody, valid);
        if (!valid) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok().build();
    }
}
