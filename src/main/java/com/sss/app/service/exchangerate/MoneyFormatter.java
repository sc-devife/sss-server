package com.sss.app.service.exchangerate;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * The one number formatter for amounts printed in documents and emails. It follows
 * the vendor's rounding setting (whole numbers vs the currency's minor units) and
 * uses Indian digit grouping for INR, standard grouping otherwise. Symbols are added
 * by the templates themselves ({{currencySymbol}}).
 */
@Component
@RequiredArgsConstructor
public class MoneyFormatter {

    private final MoneyScale moneyScale;
    private final CurrencyDisplayService currencyDisplayService;

    /** currencyCode null = the vendor's base currency. */
    public NumberFormat forOrg(Long orgId, String currencyCode) {
        String code = currencyCode != null && !currencyCode.isBlank() ? currencyCode : currencyDisplayService.baseCurrencyCode(orgId);
        NumberFormat format = NumberFormat.getInstance("INR".equalsIgnoreCase(code) ? new Locale("en", "IN") : Locale.US);
        int digits = moneyScale.customerScale(orgId, code);
        format.setMinimumFractionDigits(digits);
        format.setMaximumFractionDigits(digits);
        return format;
    }

    /** For the logged-in user's vendor, in the vendor's base currency. */
    public NumberFormat forCaller() {
        return forOrg(MoneyScale.callerOrgId(), null);
    }
}
