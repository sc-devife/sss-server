package com.sss.app.service.exchangerate;

import com.sss.app.dto.exchangerate.ExchangeRateResponseDTO;
import com.sss.app.exception.BadRequestException;
import com.sss.app.repository.OrganizationSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Converts a supplier payment made in some currency into the vendor's base
 * currency (what totals sum), keeping the original amount/currency/rate.
 */
@Component
@RequiredArgsConstructor
public class SupplierPaymentCurrencyResolver {

    /** paidAmount/paidCurrency/fxRate are null when the payment was in the base currency. */
    public record Converted(BigDecimal baseAmount, BigDecimal paidAmount, String paidCurrency, BigDecimal fxRate) {}

    private final OrganizationSettingsRepository organizationSettingsRepository;
    private final ExchangeRateService exchangeRateService;
    private final MoneyScale moneyScale;

    public Converted convert(Long orgId, BigDecimal amount, String currencyCode, BigDecimal customRate) {
        String base = organizationSettingsRepository.findById(orgId)
                .map(s -> s.getDefaultCurrencyCode())
                .filter(c -> c != null && !c.isBlank())
                .orElse("INR");
        if (currencyCode == null || currencyCode.isBlank() || currencyCode.equalsIgnoreCase(base)) {
            return new Converted(amount, null, null, null);
        }
        String code = currencyCode.toUpperCase();
        BigDecimal rate;
        if (customRate != null) {
            if (customRate.signum() <= 0) throw new BadRequestException("The exchange rate must be greater than 0");
            rate = customRate;
        } else {
            ExchangeRateResponseDTO resolved = exchangeRateService.resolve(base, code);
            rate = resolved.getRate();
        }
        return new Converted(amount.divide(rate, moneyScale.forCurrency(base), RoundingMode.HALF_UP), amount, code, rate);
    }
}
