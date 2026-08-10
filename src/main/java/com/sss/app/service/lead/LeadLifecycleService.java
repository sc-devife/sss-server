package com.sss.app.service.lead;

import com.sss.app.dto.escape.EscapeCreateRequestDTO;
import com.sss.app.dto.escape.EscapeResponseDTO;
import com.sss.app.dto.lead.LeadResponseDTO;

import java.util.UUID;

/**
 * The only way a Lead's status changes (Section 7: "editable only by
 * system" — users trigger transitions through these actions rather than
 * freeform field edits). Every transition writes an AuditLog entry.
 */
public interface LeadLifecycleService {
    LeadResponseDTO contact(UUID leadId);
    LeadResponseDTO qualify(UUID leadId);
    LeadResponseDTO disqualify(UUID leadId, String reason);
    LeadResponseDTO markLost(UUID leadId, String reason);
    LeadResponseDTO markDuplicate(UUID leadId, String reason);
    EscapeResponseDTO convertToEscape(UUID leadId, EscapeCreateRequestDTO request);

    /** Excel writeup: manual "Mark as Priority Lead" override, available regardless of auto-detection. */
    LeadResponseDTO togglePriority(UUID leadId);
}
