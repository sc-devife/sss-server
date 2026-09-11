package com.sss.app.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

// Extracted from the previously-duplicated private inrWholeFormat() helpers
// in QuotationDataService/BillingDataService — one shared formatter instead
// of a third copy for notification messages.
public final class CurrencyFormat {
    private CurrencyFormat() {}

    private static final ThreadLocal<NumberFormat> INR_WHOLE = ThreadLocal.withInitial(() -> {
        NumberFormat format = NumberFormat.getInstance(new Locale("en", "IN"));
        format.setMaximumFractionDigits(0);
        format.setMinimumFractionDigits(0);
        return format;
    });

    /** e.g. "₹25,000" */
    public static String inrWhole(BigDecimal amount) {
        return "₹" + INR_WHOLE.get().format(amount == null ? BigDecimal.ZERO : amount);
    }
}
