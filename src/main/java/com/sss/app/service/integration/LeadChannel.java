package com.sss.app.service.integration;

import java.util.List;

/**
 * The full set of lead channels from Section 7 — one row per channel
 * regardless of whether it has a real adapter yet. Adding a new channel
 * later means adding an entry here plus (eventually) a real
 * LeadChannelAdapter implementation; the UI/connection-management code
 * doesn't change.
 */
public record LeadChannel(String code, String label, boolean available) {

    public static final List<LeadChannel> ALL = List.of(
            new LeadChannel("webhook", "Generic Inbound Webhook", true),
            new LeadChannel("whatsapp", "WhatsApp Business API", false),
            new LeadChannel("instagram", "Instagram", false),
            new LeadChannel("youtube", "YouTube", false),
            new LeadChannel("google_ads", "Google Ads", false),
            new LeadChannel("agency", "Third-Party Agency Feed", false)
    );

    public static boolean isAvailable(String code) {
        return ALL.stream().anyMatch(c -> c.code().equals(code) && c.available());
    }

    public static boolean exists(String code) {
        return ALL.stream().anyMatch(c -> c.code().equals(code));
    }
}
