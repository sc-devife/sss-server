package com.sss.app.service.dashboard.impl;

import com.sss.app.dto.dashboard.DashboardOrgMetricsDTO;
import com.sss.app.dto.dashboard.DashboardResponseDTO;
import com.sss.app.dto.dashboard.LeadsTrendPointDTO;
import com.sss.app.dto.dashboard.NameCountDTO;
import com.sss.app.dto.dashboard.PaymentStatusBreakdownDTO;
import com.sss.app.dto.dashboard.QuoteAnalyticsDTO;
import com.sss.app.dto.dashboard.StatusCountDTO;
import com.sss.app.dto.escape.EscapeResponseDTO;
import com.sss.app.dto.payment.PaymentMilestoneResponseDTO;
import com.sss.app.entity.escape.EscapeStatus;
import com.sss.app.entity.lead.LeadStatus;
import com.sss.app.entity.quote.Quote;
import com.sss.app.entity.users.User;
import com.sss.app.mapper.escape.EscapeMapper;
import com.sss.app.mapper.payment.PaymentMilestoneMapper;
import com.sss.app.repository.escape.EscapeRepository;
import com.sss.app.repository.lead.LeadRepository;
import com.sss.app.repository.payment.PaymentMilestoneRepository;
import com.sss.app.repository.quote.QuoteRepository;
import com.sss.app.service.dashboard.DashboardService;
import com.sss.app.service.permissions.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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
    private static final List<String> LEAD_FUNNEL_ORDER = List.of(
            LeadStatus.NEW, LeadStatus.CONTACTED, LeadStatus.QUALIFIED, LeadStatus.CONVERTED,
            LeadStatus.UNQUALIFIED, LeadStatus.LOST, LeadStatus.DUPLICATE);
    private static final List<String> ESCAPE_PIPELINE_ORDER;
    static {
        List<String> order = new ArrayList<>(EscapeStatus.ORDER);
        order.add(EscapeStatus.CANCELLED);
        ESCAPE_PIPELINE_ORDER = order;
    }
    private static final List<String> QUOTE_STATUS_ORDER = List.of("draft", "sent", "accepted", "rejected", "superseded");
    // Every real PaymentMilestone.status value (see PaymentMilestoneHelper /
    // PaymentReminderServiceImpl) — "unverified" is deliberately kept as its
    // own bucket rather than folded into "pending", since it's a genuinely
    // different state (agent-recorded, awaiting finance confirmation).
    private static final List<String> PAYMENT_STATUS_ORDER = List.of("pending", "unverified", "partially_paid", "paid", "overdue");

    private final LeadRepository leadRepository;
    private final EscapeRepository escapeRepository;
    private final PaymentMilestoneRepository paymentMilestoneRepository;
    private final QuoteRepository quoteRepository;
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

        CompletableFuture<List<EscapeResponseDTO>> escapesFuture = CompletableFuture.supplyAsync(() ->
                escapeRepository.findAllByOrgIdAndAssignedToUserIdAndStatusNotIn(orgId, userId, INACTIVE_ESCAPE_STATUSES)
                        .stream().map(escapeMapper::toResponse).toList());

        CompletableFuture<List<PaymentMilestoneResponseDTO>> milestonesFuture = CompletableFuture.supplyAsync(() ->
                paymentMilestoneRepository.findUpcomingForAssignee(orgId, userId, OPEN_MILESTONE_STATUSES)
                        .stream().map(paymentMilestoneMapper::toResponse).toList());

        CompletableFuture<DashboardOrgMetricsDTO> orgMetricsFuture = canReadOrgMetrics
                ? CompletableFuture.supplyAsync(() -> buildOrgMetrics(orgId))
                : CompletableFuture.completedFuture(null);

        CompletableFuture.allOf(escapesFuture, milestonesFuture, orgMetricsFuture).join();

        DashboardResponseDTO response = new DashboardResponseDTO();
        response.setMyOpenEscapes(escapesFuture.join());
        response.setMyUpcomingPaymentMilestones(milestonesFuture.join());
        response.setOrgMetrics(orgMetricsFuture.join());
        return response;
    }

    // Its own queries are likewise independent of each other.
    private DashboardOrgMetricsDTO buildOrgMetrics(Long orgId) {
        LocalDateTime now = LocalDateTime.now();

        CompletableFuture<Long> leadsInLast30DaysFuture = CompletableFuture.supplyAsync(() ->
                leadRepository.countByOrgIdAndCreatedAtAfter(orgId, now.minusDays(30)));
        CompletableFuture<Long> previousPeriodLeadsFuture = CompletableFuture.supplyAsync(() ->
                leadRepository.countByOrgIdAndCreatedAtBetween(orgId, now.minusDays(60), now.minusDays(30)));
        CompletableFuture<Long> totalLeadsFuture = CompletableFuture.supplyAsync(() -> leadRepository.countByOrgId(orgId));
        CompletableFuture<Long> convertedLeadsFuture = CompletableFuture.supplyAsync(() ->
                leadRepository.countByOrgIdAndStatus(orgId, LeadStatus.CONVERTED));
        CompletableFuture<Long> escapesInProgressFuture = CompletableFuture.supplyAsync(() ->
                escapeRepository.countByOrgIdAndStatusNotIn(orgId, INACTIVE_ESCAPE_STATUSES));
        CompletableFuture<List<Object[]>> leadFunnelRawFuture = CompletableFuture.supplyAsync(() ->
                leadRepository.countByStatusGroupedForOrg(orgId));
        CompletableFuture<List<Object[]>> leadSourceRawFuture = CompletableFuture.supplyAsync(() ->
                leadRepository.countBySourceGroupedForOrg(orgId));
        CompletableFuture<List<Object[]>> escapePipelineRawFuture = CompletableFuture.supplyAsync(() ->
                escapeRepository.countByStatusGroupedForOrg(orgId));
        CompletableFuture<List<Object[]>> topEscapePointsRawFuture = CompletableFuture.supplyAsync(() ->
                escapeRepository.countActiveByEscapePoint(orgId, INACTIVE_ESCAPE_STATUSES, PageRequest.of(0, 10)));
        CompletableFuture<List<Object[]>> paymentAggRawFuture = CompletableFuture.supplyAsync(() ->
                paymentMilestoneRepository.aggregateByStatusForOrg(orgId));
        CompletableFuture<BigDecimal> previousPeriodRevenueCollectedFuture = CompletableFuture.supplyAsync(() ->
                paymentMilestoneRepository.sumPaidBetween(orgId, now.minusDays(60), now.minusDays(30)));
        CompletableFuture<List<Quote>> quotesFuture = CompletableFuture.supplyAsync(() -> quoteRepository.findAllByOrgId(orgId));

        CompletableFuture.allOf(
                leadsInLast30DaysFuture, previousPeriodLeadsFuture, totalLeadsFuture, convertedLeadsFuture,
                escapesInProgressFuture, leadFunnelRawFuture, leadSourceRawFuture, escapePipelineRawFuture,
                topEscapePointsRawFuture, paymentAggRawFuture, previousPeriodRevenueCollectedFuture, quotesFuture
        ).join();

        DashboardOrgMetricsDTO metrics = new DashboardOrgMetricsDTO();
        metrics.setLeadsInLast30Days(leadsInLast30DaysFuture.join());
        metrics.setPreviousPeriodLeadsCount(previousPeriodLeadsFuture.join());
        long totalLeads = totalLeadsFuture.join();
        long convertedLeads = convertedLeadsFuture.join();
        metrics.setConversionRatePercent(totalLeads == 0 ? 0 : (convertedLeads * 100.0) / totalLeads);
        metrics.setEscapesInProgress(escapesInProgressFuture.join());

        metrics.setLeadFunnel(toOrderedStatusCounts(leadFunnelRawFuture.join(), LEAD_FUNNEL_ORDER));
        metrics.setLeadSourceBreakdown(toSortedNameCounts(leadSourceRawFuture.join()));
        metrics.setEscapePipeline(toOrderedStatusCounts(escapePipelineRawFuture.join(), ESCAPE_PIPELINE_ORDER));
        metrics.setTopEscapePoints(toSortedNameCounts(topEscapePointsRawFuture.join()));

        applyPaymentBreakdown(metrics, paymentAggRawFuture.join());
        metrics.setPreviousPeriodRevenueCollectedInr(previousPeriodRevenueCollectedFuture.join());

        List<Quote> quotes = quotesFuture.join();
        metrics.setQuoteAnalytics(buildQuoteAnalytics(quotes));
        // The org's booked revenue — accepted quotes only, distinct from
        // quoteAnalytics.totalQuoteValueInr (every quote regardless of outcome).
        metrics.setTotalRevenueInr(quotes.stream()
                .filter(q -> "accepted".equals(q.getStatus()))
                .map(q -> q.getTotalInr() == null ? BigDecimal.ZERO : q.getTotalInr())
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        return metrics;
    }

    // Reorders a raw [status, count] groupBy result into canonical business
    // order, defaulting any status with zero rows to 0 — SQL GROUP BY only
    // returns statuses that actually occur, but a funnel/pipeline chart
    // should always show every stage.
    private List<StatusCountDTO> toOrderedStatusCounts(List<Object[]> raw, List<String> order) {
        Map<String, Long> byStatus = raw.stream()
                .collect(Collectors.toMap(row -> (String) row[0], row -> (Long) row[1]));
        List<StatusCountDTO> result = new ArrayList<>();
        for (String status : order) {
            result.add(new StatusCountDTO(status, byStatus.getOrDefault(status, 0L)));
        }
        return result;
    }

    private List<NameCountDTO> toSortedNameCounts(List<Object[]> raw) {
        return raw.stream()
                .map(row -> new NameCountDTO((String) row[0], (Long) row[1]))
                .sorted((a, b) -> Long.compare(b.getCount(), a.getCount()))
                .toList();
    }

    // The same groupBy-by-status result backs three different things: the
    // Revenue & Payments donut, the "revenue collected"/"overdue" KPI cards,
    // and — filtered to the historically-open statuses — the exact same
    // revenuePipelineInr figure the dashboard already returned before this
    // change (same statuses, same subtraction, just sourced from one shared
    // query instead of a separate full-list fetch).
    private void applyPaymentBreakdown(DashboardOrgMetricsDTO metrics, List<Object[]> raw) {
        record Bucket(long count, BigDecimal totalInr, BigDecimal paidInr) {}
        Map<String, Bucket> byStatus = new LinkedHashMap<>();
        for (Object[] row : raw) {
            BigDecimal total = row[2] == null ? BigDecimal.ZERO : (BigDecimal) row[2];
            BigDecimal paid = row[3] == null ? BigDecimal.ZERO : (BigDecimal) row[3];
            byStatus.put((String) row[0], new Bucket((Long) row[1], total, paid));
        }

        List<PaymentStatusBreakdownDTO> breakdown = new ArrayList<>();
        BigDecimal revenueCollected = BigDecimal.ZERO;
        BigDecimal revenuePipeline = BigDecimal.ZERO;
        for (String status : PAYMENT_STATUS_ORDER) {
            Bucket b = byStatus.getOrDefault(status, new Bucket(0, BigDecimal.ZERO, BigDecimal.ZERO));
            breakdown.add(new PaymentStatusBreakdownDTO(status, b.count(), b.totalInr(), b.paidInr()));
            revenueCollected = revenueCollected.add(b.paidInr());
            if (OPEN_MILESTONE_STATUSES.contains(status)) {
                revenuePipeline = revenuePipeline.add(b.totalInr().subtract(b.paidInr()));
            }
        }
        metrics.setPaymentBreakdown(breakdown);
        metrics.setRevenueCollectedInr(revenueCollected);
        metrics.setRevenuePipelineInr(revenuePipeline);

        Bucket overdue = byStatus.getOrDefault("overdue", new Bucket(0, BigDecimal.ZERO, BigDecimal.ZERO));
        metrics.setOverduePaymentsCount(overdue.count());
        metrics.setOverduePaymentsAmountInr(overdue.totalInr().subtract(overdue.paidInr()));
    }

    private QuoteAnalyticsDTO buildQuoteAnalytics(List<Quote> quotes) {
        long totalQuotes = quotes.size();
        Map<String, Long> byStatus = quotes.stream()
                .collect(Collectors.groupingBy(Quote::getStatus, Collectors.counting()));
        List<StatusCountDTO> statusBreakdown = new ArrayList<>();
        for (String status : QUOTE_STATUS_ORDER) {
            statusBreakdown.add(new StatusCountDTO(status, byStatus.getOrDefault(status, 0L)));
        }

        long accepted = byStatus.getOrDefault("accepted", 0L);
        long rejected = byStatus.getOrDefault("rejected", 0L);
        double acceptanceRate = totalQuotes == 0 ? 0 : (accepted * 100.0) / totalQuotes;

        BigDecimal totalValue = quotes.stream()
                .map(q -> q.getTotalInr() == null ? BigDecimal.ZERO : q.getTotalInr())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal averageValue = totalQuotes == 0
                ? BigDecimal.ZERO
                : totalValue.divide(BigDecimal.valueOf(totalQuotes), 2, RoundingMode.HALF_UP);

        return new QuoteAnalyticsDTO(totalQuotes, accepted, rejected, acceptanceRate, averageValue, totalValue, statusBreakdown);
    }

    @Override
    public List<LeadsTrendPointDTO> getLeadsTrend(String period) {
        if (!permissionService.hasPermission("organizations.read")) {
            return List.of();
        }
        Long orgId = currentUser().getOrgId();

        String granularity;
        int bucketCount;
        LocalDateTime since;
        LocalDateTime now = LocalDateTime.now();
        switch (period) {
            case "7d" -> { granularity = "day"; bucketCount = 7; since = now.minusDays(6).toLocalDate().atStartOfDay(); }
            case "90d" -> { granularity = "day"; bucketCount = 90; since = now.minusDays(89).toLocalDate().atStartOfDay(); }
            case "12m" -> { granularity = "month"; bucketCount = 12; since = now.minusMonths(11).withDayOfMonth(1).toLocalDate().atStartOfDay(); }
            default -> { granularity = "day"; bucketCount = 30; since = now.minusDays(29).toLocalDate().atStartOfDay(); }
        }

        List<Object[]> raw = leadRepository.countTrendForOrg(orgId, granularity, since);
        DateTimeFormatter keyFormat = DateTimeFormatter.ofPattern(granularity.equals("month") ? "yyyy-MM" : "yyyy-MM-dd");
        Map<String, Long> byBucket = new LinkedHashMap<>();
        for (Object[] row : raw) {
            LocalDateTime bucket = ((java.sql.Timestamp) row[0]).toLocalDateTime();
            byBucket.put(bucket.format(keyFormat), (Long) row[1]);
        }

        // Gap-fill every expected bucket to 0 so the trend chart's x-axis is
        // never missing a point just because no lead happened to land there.
        List<LeadsTrendPointDTO> points = new ArrayList<>();
        for (int i = bucketCount - 1; i >= 0; i--) {
            LocalDateTime bucketStart = granularity.equals("month") ? now.minusMonths(i).withDayOfMonth(1) : now.minusDays(i);
            String key = bucketStart.format(keyFormat);
            points.add(new LeadsTrendPointDTO(key, byBucket.getOrDefault(key, 0L)));
        }
        return points;
    }
}
