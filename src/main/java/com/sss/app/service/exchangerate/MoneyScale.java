package com.sss.app.service.exchangerate;

import com.sss.app.entity.system.Currency;
import com.sss.app.repository.system.CurrencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * How many decimal places (ISO 4217 minor units) an amount in a currency has:
 * 0 for JPY, 2 for INR/AED/USD, 3 for KWD/BHD/OMR. Money columns are stored with
 * room for 4 decimals (V138); every amount the app computes is rounded to its
 * currency's own scale here, so nothing is silently rounded to two decimals and
 * three-decimal currencies keep their last digit.
 */
@Component
@RequiredArgsConstructor
public class MoneyScale {

    private static final int DEFAULT_SCALE = 2;

    private final CurrencyRepository currencyRepository;
    private final CurrencyDisplayService currencyDisplayService;
    private final com.sss.app.repository.OrganizationSettingsRepository organizationSettingsRepository;
    private final Map<String, Integer> cache = new ConcurrentHashMap<>();

    public int forCurrency(String currencyCode) {
        if (currencyCode == null || currencyCode.isBlank()) return DEFAULT_SCALE;
        return cache.computeIfAbsent(currencyCode.toUpperCase(), code -> currencyRepository.findAll().stream()
                .filter(c -> code.equalsIgnoreCase(c.getCode()))
                .map(Currency::getMinor_units)
                .filter(u -> u != null)
                .findFirst()
                .orElse(DEFAULT_SCALE));
    }

    /** Scale of the vendor's base currency. */
    public int forOrg(Long orgId) {
        return forCurrency(currencyDisplayService.baseCurrencyCode(orgId));
    }

    /** True when the vendor chose "round to whole numbers" (Organization settings). */
    public boolean isWhole(Long orgId) {
        if (orgId == null) return false;
        return organizationSettingsRepository.findById(orgId)
                .map(s -> "whole".equals(s.getRoundingMode()))
                .orElse(false);
    }

    /**
     * Scale for customer-facing amounts (quote pricing, the figures printed on
     * quotations/invoices): 0 when the vendor rounds to whole numbers, otherwise
     * the currency's own minor units. Supplier costs and payouts keep exact scale.
     */
    public int customerScale(Long orgId, String currencyCode) {
        return isWhole(orgId) ? 0 : forCurrency(currencyCode);
    }

    public int customerScaleForOrg(Long orgId) {
        return customerScale(orgId, currencyDisplayService.baseCurrencyCode(orgId));
    }

    /** The logged-in user's org, or null outside a request (async/scheduled work). */
    public static Long callerOrgId() {
        try {
            var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof com.sss.app.entity.users.User user) return user.getOrgId();
        } catch (Exception ignored) {
            // no security context
        }
        return null;
    }

    public BigDecimal round(BigDecimal amount, int scale) {
        return amount == null ? null : amount.setScale(scale, RoundingMode.HALF_UP);
    }
}
