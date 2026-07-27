package com.sss.app.service.assignment;

import com.sss.app.dto.lead.LeadResponseDTO;
import com.sss.app.entity.lead.Lead;

/**
 * Section 5: configurable assignment strategies (specialist match, load
 * balancing, capacity cap, manual override), invoked automatically on lead
 * creation. If no eligible candidate is found the lead is left unassigned
 * (the "unassigned queue" is just "no assignedToUserId set", not a separate
 * table — a Lead Assigner picks it up manually via manuallyAssign).
 */
public interface LeadAssignmentService {
    void autoAssign(Lead lead);

    LeadResponseDTO manuallyAssign(Long leadId, Long userId, String reason);
}
