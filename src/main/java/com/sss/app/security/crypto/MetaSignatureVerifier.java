package com.sss.app.security.crypto;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

/**
 * Verifies Meta's X-Hub-Signature-256 webhook header: HMAC-SHA256 over the
 * exact raw request body, using the Meta App Secret, compared in constant
 * time. The caller MUST pass the untouched raw body string — re-serializing
 * a parsed payload before verifying will make every check fail on
 * whitespace/key-order differences from what Meta actually signed.
 */
@Component
public class MetaSignatureVerifier {

    private static final String SIGNATURE_PREFIX = "sha256=";

    @Value("${META_APP_SECRET:}")
    private String appSecret;

    public boolean isValid(String rawBody, String signatureHeader) {
        if (signatureHeader == null || !signatureHeader.startsWith(SIGNATURE_PREFIX)) {
            return false;
        }
        if (appSecret == null || appSecret.isBlank()) {
            return false;
        }
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(appSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] computed = mac.doFinal(rawBody.getBytes(StandardCharsets.UTF_8));
            String computedHex = HexFormat.of().formatHex(computed);
            String provided = signatureHeader.substring(SIGNATURE_PREFIX.length());
            return MessageDigest.isEqual(
                    computedHex.getBytes(StandardCharsets.UTF_8),
                    provided.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return false;
        }
    }
}
