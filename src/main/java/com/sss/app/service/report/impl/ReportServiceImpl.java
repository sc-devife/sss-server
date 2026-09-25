package com.sss.app.service.report.impl;

import com.sss.app.dto.report.SalesReportResponseDTO;
import com.sss.app.dto.report.SalesReportRowDTO;
import com.sss.app.entity.escape.EscapeStatus;
import com.sss.app.entity.library.escapepoint.EscapePoint;
import com.sss.app.entity.users.User;
import com.sss.app.exception.BadRequestException;
import com.sss.app.repository.UserRepository;
import com.sss.app.repository.library.escapepoint.EscapePointRepository;
import com.sss.app.repository.report.SalesReportRepository;
import com.sss.app.repository.report.SalesReportRepository.Grouping;
import com.sss.app.service.report.ReportService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import com.sss.app.service.exchangerate.MoneyScale;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Sales reports. Cohort = the caller's org's non-archived leads created in the
 * requested period. Leads = size of the cohort per group; Completed / Hold /
 * Cancelled = how many of that cohort's escapes are in that Escape status
 * (EscapeStatus constants — no separate definitions); Revenue = accepted quotes'
 * total, the same figure the Dashboard reports as booked revenue.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private final com.sss.app.service.exchangerate.MoneyScale moneyScale;

    private static final List<String> REPORT_STATUSES =
            List.of(EscapeStatus.COMPLETED, EscapeStatus.HOLD, EscapeStatus.CANCELLED);

    // Direct-lead channels -> display labels (same set as the Leads page's Source filter).
    private static final Map<String, String> CHANNEL_LABELS = Map.of(
            "manual", "Manual",
            "whatsapp", "WhatsApp",
            "instagram", "Instagram",
            "youtube", "YouTube",
            "google_ads", "Google Ads");

    private final SalesReportRepository salesReportRepository;
    private final UserRepository userRepository;
    private final EscapePointRepository escapePointRepository;

    private User currentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private static Grouping groupingFor(String type) {
        return switch (type == null ? "" : type) {
            case "sales-persons" -> Grouping.SALES_PERSON;
            case "escape-points" -> Grouping.ESCAPE_POINT;
            case "lead-sources" -> Grouping.LEAD_SOURCE;
            default -> throw new BadRequestException("Unknown report type: " + type);
        };
    }

    @Override
    public SalesReportResponseDTO getSalesReport(String type, LocalDate from, LocalDate to) {
        Grouping grouping = groupingFor(type);
        Long orgId = currentUser().getOrgId();
        // Inclusive dates, whole days — same convention as the Leads list's from/to.
        LocalDateTime start = from != null ? from.atStartOfDay() : LocalDateTime.of(1900, 1, 1, 0, 0);
        LocalDateTime end = to != null ? to.plusDays(1).atStartOfDay() : LocalDateTime.of(2999, 1, 1, 0, 0);

        Map<Object, Long> leads = salesReportRepository.countLeads(grouping, orgId, start, end);
        Map<Object, Map<String, Long>> escapes =
                salesReportRepository.countEscapesByStatus(grouping, orgId, start, end, REPORT_STATUSES);
        boolean hasRevenue = grouping != Grouping.SALES_PERSON;
        Map<Object, BigDecimal> revenue = hasRevenue
                ? salesReportRepository.sumAcceptedQuoteRevenue(grouping, orgId, start, end)
                : Map.of();

        Map<Object, String> labels = resolveLabels(grouping, leads.keySet());
        List<SalesReportRowDTO> rows = new ArrayList<>();
        for (Map.Entry<Object, Long> entry : leads.entrySet()) {
            Object key = entry.getKey();
            Map<String, Long> byStatus = escapes.getOrDefault(key, Map.of());
            rows.add(new SalesReportRowDTO(
                    labels.get(key),
                    entry.getValue(),
                    byStatus.getOrDefault(EscapeStatus.COMPLETED, 0L),
                    byStatus.getOrDefault(EscapeStatus.HOLD, 0L),
                    byStatus.getOrDefault(EscapeStatus.CANCELLED, 0L),
                    hasRevenue ? revenue.getOrDefault(key, BigDecimal.ZERO) : null));
        }
        rows.sort(Comparator.comparingLong(SalesReportRowDTO::getLeads).reversed()
                .thenComparing(SalesReportRowDTO::getLabel, String.CASE_INSENSITIVE_ORDER));
        return new SalesReportResponseDTO(type, from, to, hasRevenue, rows);
    }

    private Map<Object, String> resolveLabels(Grouping grouping, Set<Object> keys) {
        Map<Object, String> labels = new HashMap<>();
        switch (grouping) {
            case SALES_PERSON -> {
                Set<Long> ids = keys.stream().filter(k -> k != null).map(k -> ((Number) k).longValue())
                        .collect(Collectors.toCollection(HashSet::new));
                Map<Long, User> users = userRepository.findAllById(ids).stream()
                        .collect(Collectors.toMap(User::getSeqp, u -> u));
                for (Object key : keys) {
                    if (key == null) {
                        labels.put(null, "Unassigned");
                        continue;
                    }
                    User u = users.get(((Number) key).longValue());
                    labels.put(key, u == null ? "Unknown user" : displayName(u));
                }
            }
            case ESCAPE_POINT -> {
                Set<Long> ids = keys.stream().map(k -> ((Number) k).longValue()).collect(Collectors.toSet());
                Map<Long, String> names = escapePointRepository.findAllById(ids).stream()
                        .collect(Collectors.toMap(EscapePoint::getSeqp, EscapePoint::getName));
                for (Object key : keys) labels.put(key, names.getOrDefault(((Number) key).longValue(), "Unknown"));
            }
            case LEAD_SOURCE -> {
                for (Object key : keys) {
                    String raw = String.valueOf(key);
                    labels.put(key, CHANNEL_LABELS.getOrDefault(raw, raw));
                }
            }
        }
        return labels;
    }

    // `name` is the login username (the email); the person's name is first + last.
    private static String displayName(User u) {
        String full = ((u.getFirst_name() == null ? "" : u.getFirst_name()) + " "
                + (u.getLast_name() == null ? "" : u.getLast_name())).trim();
        return full.isEmpty() ? u.getName() : full;
    }

    @Override
    public byte[] exportSalesReportCsv(String type, LocalDate from, LocalDate to) {
        SalesReportResponseDTO report = getSalesReport(type, from, to);
        String firstHeader = switch (type) {
            case "sales-persons" -> "Sales Person";
            case "escape-points" -> "Escape Point";
            default -> "Lead Source";
        };
        List<String> header = new ArrayList<>(List.of(firstHeader, "Leads", "Completed", "Hold", "Cancelled"));
        if (report.isHasRevenue()) header.add("Revenue");

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
             CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT)) {
            writer.write('﻿'); // BOM so Excel opens it as UTF-8
            printer.printRecord(header);
            for (SalesReportRowDTO r : report.getRows()) {
                List<Object> record = new ArrayList<>(List.of(r.getLabel(), r.getLeads(), r.getCompleted(), r.getHold(), r.getCancelled()));
                if (report.isHasRevenue()) record.add(r.getRevenue().setScale(moneyScale.forOrg(MoneyScale.callerOrgId()), RoundingMode.HALF_UP).toPlainString());
                printer.printRecord(record);
            }
            printer.flush();
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to build report CSV", e);
        }
    }
}
