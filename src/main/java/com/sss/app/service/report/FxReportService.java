package com.sss.app.service.report;

import com.sss.app.dto.report.FxReportResponseDTO;
import com.sss.app.entity.payment.PaymentRecord;
import com.sss.app.entity.users.User;
import com.sss.app.repository.payment.PaymentRecordRepository;
import com.sss.app.service.exchangerate.CurrencyDisplayService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Payments received in a currency other than the vendor's base, and the FX gain/loss they carried. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FxReportService {

    private final PaymentRecordRepository paymentRecordRepository;
    private final CurrencyDisplayService currencyDisplayService;

    public FxReportResponseDTO getFxReport(LocalDate from, LocalDate to) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long orgId = user.getOrgId();
        String base = currencyDisplayService.baseCurrencyCode(orgId);

        Map<String, FxReportResponseDTO.Row> byCurrency = new TreeMap<>();
        for (PaymentRecord r : paymentRecordRepository.findAllByOrgId(orgId)) {
            if (base.equalsIgnoreCase(r.getReceivedCurrency())) continue;
            LocalDate day = r.getRecordedAt().toLocalDate();
            if ((from != null && day.isBefore(from)) || (to != null && day.isAfter(to))) continue;
            FxReportResponseDTO.Row row = byCurrency.computeIfAbsent(r.getReceivedCurrency(),
                    c -> new FxReportResponseDTO.Row(c, 0, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
            row.setPayments(row.getPayments() + 1);
            row.setReceivedTotal(row.getReceivedTotal().add(r.getReceivedAmount()));
            row.setCreditedBase(row.getCreditedBase().add(r.getAppliedAmountBase()));
            row.setValueAtReceiptBase(row.getValueAtReceiptBase().add(r.getBaseValueReceived()));
            row.setNetFxBase(row.getNetFxBase().add(r.getFxDifferenceBase()));
        }
        List<FxReportResponseDTO.Row> rows = new ArrayList<>(byCurrency.values());
        BigDecimal net = rows.stream().map(FxReportResponseDTO.Row::getNetFxBase).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new FxReportResponseDTO(base, rows, net);
    }
}
