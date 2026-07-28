package com.sss.app.dto.dashboard;

import com.sss.app.dto.escape.EscapeResponseDTO;
import com.sss.app.dto.lead.LeadResponseDTO;
import com.sss.app.dto.payment.PaymentMilestoneResponseDTO;
import lombok.Data;

import java.util.List;

@Data
public class DashboardResponseDTO {
    /** Null when the caller doesn't hold organizations.read (Regular Users). */
    private DashboardOrgMetricsDTO orgMetrics;

    // Section 4: "Regular Users see their assigned leads/trips/tasks and
    // upcoming payment milestones" — populated for every caller regardless
    // of role, since an Admin can also be a working agent with assignments.
    private List<LeadResponseDTO> myOpenLeads;
    private List<EscapeResponseDTO> myOpenTrips;
    private List<PaymentMilestoneResponseDTO> myUpcomingPaymentMilestones;
}
