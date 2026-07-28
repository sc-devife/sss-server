package com.sss.app.service.escape;

import com.sss.app.dto.escape.EscapeResponseDTO;

/**
 * The only way a Trip's (Escape's) status changes (Section 8) — validated
 * against TripStatus.ORDER and audited, mirroring Lead's lifecycle actions
 * from Phase 3. No generic PATCH/PUT on status exists.
 */
public interface TripLifecycleService {
    EscapeResponseDTO advance(Long tripId, String targetStatus);
    EscapeResponseDTO cancel(Long tripId, String reason);
}
