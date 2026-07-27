package com.sss.app.service.lead;

import com.sss.app.dto.escape.EscapeCreateRequestDTO;
import com.sss.app.dto.escape.EscapeResponseDTO;
import com.sss.app.dto.lead.LeadResponseDTO;

/**
 * The only way a Lead's status changes (Section 7: "editable only by
 * system" — users trigger transitions through these actions rather than
 * freeform field edits). Every transition writes an AuditLog entry.
 */
public interface LeadLifecycleService {
    LeadResponseDTO contact(Long leadId);
    LeadResponseDTO qualify(Long leadId);
    LeadResponseDTO disqualify(Long leadId, String reason);
    LeadResponseDTO markLost(Long leadId, String reason);
    LeadResponseDTO markDuplicate(Long leadId, String reason);
    EscapeResponseDTO convertToEscape(Long leadId, EscapeCreateRequestDTO request);
}
