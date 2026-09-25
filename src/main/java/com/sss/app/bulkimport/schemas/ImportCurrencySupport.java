package com.sss.app.bulkimport.schemas;

import com.sss.app.repository.system.CurrencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** Shared handling of the optional `priceCurrency` column on the hotel/activity/transport imports. */
@Component
@RequiredArgsConstructor
public class ImportCurrencySupport {

    private final CurrencyRepository currencyRepository;

    /** Blank is fine (= the vendor's base currency); anything else must be a supported currency code. */
    public Optional<String> validate(String priceCurrency) {
        if (priceCurrency == null || priceCurrency.isBlank()) return Optional.empty();
        boolean known = currencyRepository.findAll().stream()
                .anyMatch(c -> priceCurrency.trim().equalsIgnoreCase(c.getCode()));
        return known ? Optional.empty() : Optional.of("Unsupported price currency \"" + priceCurrency.trim() + "\" - use an ISO code such as USD or AED");
    }

    /** Upper-cased code, or null when blank (null = base currency). */
    public String normalize(String priceCurrency) {
        return priceCurrency == null || priceCurrency.isBlank() ? null : priceCurrency.trim().toUpperCase();
    }
}
