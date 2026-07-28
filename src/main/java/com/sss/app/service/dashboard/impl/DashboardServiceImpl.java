package com.sss.app.service.dashboard.impl;

import com.sss.app.dto.dashboard.DashboardOrgMetricsDTO;
import com.sss.app.dto.dashboard.DashboardResponseDTO;
import com.sss.app.entity.escape.TripStatus;
import com.sss.app.entity.lead.LeadStatus;
import com.sss.app.entity.users.User;
import com.sss.app.mapper.escape.EscapeMapper;
import com.sss.app.mapper.lead.LeadMapper;
import com.sss.app.mapper.payment.PaymentMilestoneMapper;
import com.sss.app.repository.escape.EscapeRepository;
import com.sss.app.repository.lead.LeadRepository;
import com.sss.app.repository.payment.PaymentMilestoneRepository;
import com.sss.app.service.dashboard.DashboardService;
import com.sss.app.service.permissions.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Section 4: role-aware Home/Dashboard. Org-wide metrics are gated behind
 * organizations.read (the same permission Admin/Super Admin already hold
 * for the Organization screens) rather than trusting the frontend alone to
 * hide them — revenue pipeline and conversion rate are sensitive.
 */
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private static final List<String> OPEN_MILESTONE_STATUSES = List.of("pending", "partially_paid", "overdue");
    private static final List<String> INACTIVE_TRIP_STATUSES = List.of(TripStatus.COMPLETED, TripStatus.CANCELLED);

    private final LeadRepository leadRepository;
    private final EscapeRepository escapeRepository;
    private final PaymentMilestoneRepository paymentMilestoneRepository;
    private final LeadMapper leadMapper;
    private final EscapeMapper escapeMapper;
    private final PaymentMilestoneMapper paymentMilestoneMapper;
    private final PermissionService permissionService;

    private User currentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Override
    public DashboardResponseDTO getDashboard() {
        User user = currentUser();
        Long orgId = user.getOrgId();
        Long userId = user.getSeqp();

        DashboardResponseDTO response = new DashboardResponseDTO();

        response.setMyOpenLeads(leadRepository.findAllByOrgIdAndAssignedToUserIdAndStatusNotIn(orgId, userId, LeadStatus.TERMINAL)
                .stream().map(leadMapper::toResponse).toList());

        response.setMyOpenTrips(escapeRepository.findAllByOrgIdAndLead_AssignedToUserIdAndStatusNotIn(orgId, userId, INACTIVE_TRIP_STATUSES)
                .stream().map(escapeMapper::toResponse).toList());

        response.setMyUpcomingPaymentMilestones(paymentMilestoneRepository.findUpcomingForAssignee(orgId, userId, OPEN_MILESTONE_STATUSES)
                .stream().map(paymentMilestoneMapper::toResponse).toList());

        if (permissionService.hasPermission("organizations.read")) {
            response.setOrgMetrics(buildOrgMetrics(orgId));
        }

        return response;
    }

    private DashboardOrgMetricsDTO buildOrgMetrics(Long orgId) {
        DashboardOrgMetricsDTO metrics = new DashboardOrgMetricsDTO();

        metrics.setLeadsInLast30Days(leadRepository.countByOrgIdAndCreatedAtAfter(orgId, LocalDateTime.now().minusDays(30)));

        long totalLeads = leadRepository.countByOrgId(orgId);
        long convertedLeads = leadRepository.countByOrgIdAndStatus(orgId, LeadStatus.CONVERTED);
        metrics.setConversionRatePercent(totalLeads == 0 ? 0 : (convertedLeads * 100.0) / totalLeads);

        metrics.setTripsInProgress(escapeRepository.countByOrgIdAndStatusNotIn(orgId, INACTIVE_TRIP_STATUSES));

        BigDecimal outstanding = paymentMilestoneRepository.findAllByOrgIdAndStatusIn(orgId, OPEN_MILESTONE_STATUSES).stream()
                .map(m -> m.getAmountUsd().subtract(m.getAmountPaidUsd()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        metrics.setRevenuePipelineUsd(outstanding);

        return metrics;
    }
}
