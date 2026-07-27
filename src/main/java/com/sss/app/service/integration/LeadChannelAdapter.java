package com.sss.app.service.integration;

import java.util.Map;

/**
 * Pluggable per-channel adapter (Section 7): connect (store credentials/
 * secret), normalize (map the channel's raw payload into the common
 * contract), acknowledge (tell the source system this lead was received).
 * "Listen/poll" isn't a method here — for a webhook channel, receiving the
 * HTTP POST *is* listening; a future polling-based channel would implement
 * its own scheduled trigger that calls normalize() the same way.
 */
public interface LeadChannelAdapter {

    String channelCode();

    NormalizedLeadPayload normalize(Map<String, Object> rawPayload);

    /** No-op for a synchronous webhook (the HTTP 200 response is the ack) — meaningful for polling-based channels that need to mark a remote item as consumed. */
    default void acknowledge(String sourceRefId) {
    }
}
