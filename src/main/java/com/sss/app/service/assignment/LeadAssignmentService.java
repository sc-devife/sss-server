package com.sss.app.service.assignment;

import com.sss.app.dto.escape.EscapeResponseDTO;
import com.sss.app.entity.escape.Escape;

import java.util.UUID;

/**
 * Section 5: configurable assignment strategies (specialist match, load
 * balancing, capacity cap, manual override) — invoked once, at the moment a
 * Lead is converted to an Escape (see EscapeHelper.createEscape), not at lead
 * creation. Leads are never individually assigned; any eligible user can work
 * any lead. If no eligible candidate is found the escape is left unassigned
 * (the "unassigned queue" is just "no assignedToUserId set" on the Escape,
 * not a separate table — a Lead Assigner picks it up manually via
 * manuallyAssign).
 */
public interface LeadAssignmentService {
    void autoAssign(Escape escape);

    EscapeResponseDTO manuallyAssign(UUID escapeId, Long userId, String reason);
}
