package com.sss.app.service.integration;

import lombok.Data;

import java.time.LocalDate;
import java.util.Map;

/**
 * The common inbound lead contract every channel adapter maps its own
 * payload shape into (Section 7) — core lead-creation logic only ever sees
 * this, never a channel's raw format.
 */
@Data
public class NormalizedLeadPayload {
    private String name;
    private String email;
    private String phone;
    private String destinationHint; // free-text destination as the channel sent it
    private LocalDate travelDate;
    private Integer numberOfPeople;
    private Integer durationDays;
    private String sourceRefId; // the channel's own id for this lead, for dedup/ack
    private Map<String, Object> rawPayload;
}
