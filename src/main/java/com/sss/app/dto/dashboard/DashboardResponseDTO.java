package com.sss.app.dto.dashboard;

import com.sss.app.dto.payment.PaymentMilestoneResponseDTO;
import lombok.Data;

import java.util.List;

@Data
public class DashboardResponseDTO {
    /** Null when the caller doesn't hold organizations.read (Regular Users). */
    private DashboardOrgMetricsDTO orgMetrics;

    // Section 4: "Regular Users see their assigned escapes/tasks and
    // upcoming payment milestones" — populated for every caller regardless
    // of role, since an Admin can also be a working agent with assignments.
    // Leads have no per-user assignee (assignment happens once, on the
    // Escape, at conversion time), so there is no "myOpenLeads" here.
    // The 3 most recently created, via a lightweight summary DTO rather than
    // the full EscapeResponseDTO — see DashboardEscapeSummaryDTO.
    private List<DashboardEscapeSummaryDTO> myOpenEscapes;
    private List<PaymentMilestoneResponseDTO> myUpcomingPaymentMilestones;
}
