package com.sss.app.service.dashboard.impl;

import com.sss.app.dto.dashboard.DashboardOrgMetricsDTO;
import com.sss.app.dto.dashboard.DashboardResponseDTO;
import com.sss.app.dto.escape.EscapeResponseDTO;
import com.sss.app.dto.lead.LeadResponseDTO;
import com.sss.app.dto.payment.PaymentMilestoneResponseDTO;
import com.sss.app.entity.escape.EscapeStatus;
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
import java.util.concurrent.CompletableFuture;

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
    private static final List<String> INACTIVE_ESCAPE_STATUSES = List.of(EscapeStatus.COMPLETED, EscapeStatus.CANCELLED);

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

    // getDashboard() previously ran 3-8 independent, unrelated read queries
    // one after another on a single thread — none of them depend on each
    // other's result, so their latency was purely additive. Running them
    // concurrently via CompletableFuture (backed by the JVM's common
    // ForkJoinPool — no new executor/bean needed) cuts total latency down to
    // roughly the slowest single query instead of the sum of all of them.
    // Safe here because: (1) this class has no @Transactional, so each
    // derived repository call already opens/commits its own short
    // transaction rather than sharing one persistence context across
    // threads, and (2) every value the async tasks need (orgId, userId, the
    // permission check) is resolved on the calling thread first — nothing
    // inside the async lambdas touches the request-scoped
    // SecurityContextHolder, which is a ThreadLocal and would not be visible
    // to a worker thread.
    @Override
    public DashboardResponseDTO getDashboard() {
        User user = currentUser();
        Long orgId = user.getOrgId();
        Long userId = user.getSeqp();
        boolean canReadOrgMetrics = permissionService.hasPermission("organizations.read");

        CompletableFuture<List<LeadResponseDTO>> leadsFuture = CompletableFuture.supplyAsync(() ->
                leadRepository.findAllByOrgIdAndAssignedToUserIdAndStatusNotIn(orgId, userId, LeadStatus.TERMINAL)
                        .stream().map(leadMapper::toResponse).toList());

        CompletableFuture<List<EscapeResponseDTO>> escapesFuture = CompletableFuture.supplyAsync(() ->
                escapeRepository.findAllByOrgIdAndLead_AssignedToUserIdAndStatusNotIn(orgId, userId, INACTIVE_ESCAPE_STATUSES)
                        .stream().map(escapeMapper::toResponse).toList());

        CompletableFuture<List<PaymentMilestoneResponseDTO>> milestonesFuture = CompletableFuture.supplyAsync(() ->
                paymentMilestoneRepository.findUpcomingForAssignee(orgId, userId, OPEN_MILESTONE_STATUSES)
                        .stream().map(paymentMilestoneMapper::toResponse).toList());

        CompletableFuture<DashboardOrgMetricsDTO> orgMetricsFuture = canReadOrgMetrics
                ? CompletableFuture.supplyAsync(() -> buildOrgMetrics(orgId))
                : CompletableFuture.completedFuture(null);

        CompletableFuture.allOf(leadsFuture, escapesFuture, milestonesFuture, orgMetricsFuture).join();

        DashboardResponseDTO response = new DashboardResponseDTO();
        response.setMyOpenLeads(leadsFuture.join());
        response.setMyOpenEscapes(escapesFuture.join());
        response.setMyUpcomingPaymentMilestones(milestonesFuture.join());
        response.setOrgMetrics(orgMetricsFuture.join());
        return response;
    }

    // Its own 4 queries are likewise independent of each other.
    private DashboardOrgMetricsDTO buildOrgMetrics(Long orgId) {
        CompletableFuture<Long> leadsInLast30DaysFuture = CompletableFuture.supplyAsync(() ->
                leadRepository.countByOrgIdAndCreatedAtAfter(orgId, LocalDateTime.now().minusDays(30)));
        CompletableFuture<Long> totalLeadsFuture = CompletableFuture.supplyAsync(() -> leadRepository.countByOrgId(orgId));
        CompletableFuture<Long> convertedLeadsFuture = CompletableFuture.supplyAsync(() ->
                leadRepository.countByOrgIdAndStatus(orgId, LeadStatus.CONVERTED));
        CompletableFuture<Long> escapesInProgressFuture = CompletableFuture.supplyAsync(() ->
                escapeRepository.countByOrgIdAndStatusNotIn(orgId, INACTIVE_ESCAPE_STATUSES));
        CompletableFuture<BigDecimal> outstandingFuture = CompletableFuture.supplyAsync(() ->
                paymentMilestoneRepository.findAllByOrgIdAndStatusIn(orgId, OPEN_MILESTONE_STATUSES).stream()
                        .map(m -> m.getAmountUsd().subtract(m.getAmountPaidUsd()))
                        .reduce(BigDecimal.ZERO, BigDecimal::add));

        CompletableFuture.allOf(
                leadsInLast30DaysFuture, totalLeadsFuture, convertedLeadsFuture, escapesInProgressFuture, outstandingFuture
        ).join();

        DashboardOrgMetricsDTO metrics = new DashboardOrgMetricsDTO();
        metrics.setLeadsInLast30Days(leadsInLast30DaysFuture.join());
        long totalLeads = totalLeadsFuture.join();
        long convertedLeads = convertedLeadsFuture.join();
        metrics.setConversionRatePercent(totalLeads == 0 ? 0 : (convertedLeads * 100.0) / totalLeads);
        metrics.setEscapesInProgress(escapesInProgressFuture.join());
        metrics.setRevenuePipelineUsd(outstandingFuture.join());
        return metrics;
    }
}
