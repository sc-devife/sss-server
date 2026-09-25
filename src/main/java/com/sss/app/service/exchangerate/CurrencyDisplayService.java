package com.sss.app.service.exchangerate;

import com.sss.app.entity.system.Currency;
import com.sss.app.repository.OrganizationSettingsRepository;
import com.sss.app.repository.system.CurrencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Looks up a vendor's base currency and a currency's display symbol, for documents and emails. */
@Component
@RequiredArgsConstructor
public class CurrencyDisplayService {

    public static final String DEFAULT_CODE = "INR";

    private final OrganizationSettingsRepository organizationSettingsRepository;
    private final CurrencyRepository currencyRepository;

    public String baseCurrencyCode(Long orgId) {
        if (orgId == null) return DEFAULT_CODE;
        return organizationSettingsRepository.findById(orgId)
                .map(s -> s.getDefaultCurrencyCode())
                .filter(c -> c != null && !c.isBlank())
                .orElse(DEFAULT_CODE);
    }

    /** The currency's symbol ("₹", "$", "د.إ"), falling back to its code when none is stored. */
    public String symbol(String currencyCode) {
        if (currencyCode == null || currencyCode.isBlank()) return "₹";
        return currencyRepository.findAll().stream()
                .filter(c -> currencyCode.equalsIgnoreCase(c.getCode()))
                .map(Currency::getSymbol)
                .filter(sym -> sym != null && !sym.isBlank())
                .findFirst()
                .orElse(currencyCode.toUpperCase());
    }

    public String baseSymbol(Long orgId) {
        return symbol(baseCurrencyCode(orgId));
    }
}
