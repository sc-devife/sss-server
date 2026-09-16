package com.sss.app.service.assignment;

import com.sss.app.dto.escape.EscapeResponseDTO;
import com.sss.app.entity.escape.Escape;
import com.sss.app.entity.lead.Lead;

import java.util.UUID;

/**
 * Section 5 / Assignment Rules: weighted-scoring candidate selection
 * (priority/specialist/large-group/language preferences, capacity cap, load
 * balancing, manual override) — see LeadAssignmentServiceImpl's class doc for
 * the scoring model. Runs at two points:
 *
 * <ul>
 *   <li>{@link #autoAssignLead}, once, at Lead intake (see
 *       LeadsHelper.createLead and the channel-intake variants) — routes the
 *       Lead itself to an agent before it's ever converted.</li>
 *   <li>{@link #autoAssign}, once, when a Lead is converted to an Escape
 *       (see EscapeHelper.createEscape) — if the source Lead already carries
 *       its own assignment, that's carried over as-is; otherwise the engine
 *       picks fresh here (the pre-Lead-routing behavior, still the fallback
 *       for leads created before this existed or with auto-assign off at
 *       intake).</li>
 * </ul>
 *
 * If no eligible candidate is found, the Lead/Escape is left unassigned (the
 * "unassigned queue" is just "no assignedToUserId set", not a separate
 * table) — a Lead Assigner picks it up manually via manuallyAssignLead /
 * manuallyAssign.
 */
public interface LeadAssignmentService {
    void autoAssignLead(Lead lead);

    void autoAssign(Escape escape);

    Lead manuallyAssignLead(UUID leadId, Long userId, String reason);

    EscapeResponseDTO manuallyAssign(UUID escapeId, Long userId, String reason);
}
