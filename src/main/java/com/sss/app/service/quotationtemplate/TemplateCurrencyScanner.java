package com.sss.app.service.quotationtemplate;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Looks for a currency symbol typed straight into an uploaded quotation/invoice
 * template. Those amounts would print in that currency whatever the quote or
 * vendor uses, so the uploader is told to use the placeholders instead.
 */
public final class TemplateCurrencyScanner {

    private TemplateCurrencyScanner() {}

    public static List<String> scan(MultipartFile file) {
        List<String> warnings = new ArrayList<>();
        if (file == null || file.isEmpty()) return warnings;
        String html;
        try {
            html = new String(file.getBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return warnings;
        }
        String lower = html.toLowerCase();
        if (html.contains("₹") || lower.contains("&#8377;") || lower.contains("&#x20b9;") || lower.contains("&#x20b9")) {
            warnings.add("This template has a hard-coded rupee sign (₹). It is replaced automatically with the right currency when "
                    + "it sits before a {{pricing...}} or other amount, but it is safer to use {{pricing.currencySymbol}} "
                    + "(the quote's currency) or {{currencySymbol}} (your base currency) yourself.");
        }
        return warnings;
    }
}
