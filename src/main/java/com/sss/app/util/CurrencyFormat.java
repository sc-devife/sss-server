package com.sss.app.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

// Extracted from the previously-duplicated private moneyFormat() helpers
// in QuotationDataService/BillingDataService — one shared formatter instead
// of a third copy for notification messages.
public final class CurrencyFormat {
    private CurrencyFormat() {}

    private static final ThreadLocal<NumberFormat> WHOLE = ThreadLocal.withInitial(() -> {
        NumberFormat format = NumberFormat.getInstance(new Locale("en", "IN"));
        format.setMaximumFractionDigits(0);
        format.setMinimumFractionDigits(0);
        return format;
    });

    /** e.g. "₹25,000" - the symbol is the vendor's base-currency symbol. */
    public static String whole(String symbol, BigDecimal amount) {
        return symbol + WHOLE.get().format(amount == null ? BigDecimal.ZERO : amount);
    }
}
