package com.sss.app.service.escape;

import com.sss.app.dto.escape.EscapeResponseDTO;

import java.time.LocalDate;
import java.util.UUID;

/**
 * The only way an Escape's status changes (Section 8) — validated
 * against EscapeStatus.ORDER and audited, mirroring Lead's lifecycle actions
 * from Phase 3. No generic PATCH/PUT on status exists.
 */
public interface EscapeLifecycleService {
    EscapeResponseDTO advance(UUID escapeId, String targetStatus);
    EscapeResponseDTO cancel(UUID escapeId, String reason);
    // Idempotent: sets status to Hold + the given date the first time, and
    // just updates the date on subsequent calls while already on Hold.
    EscapeResponseDTO hold(UUID escapeId, LocalDate holdDate);
}
