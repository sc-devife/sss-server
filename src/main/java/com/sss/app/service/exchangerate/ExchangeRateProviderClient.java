package com.sss.app.service.exchangerate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Free rate sources, both quoted against USD. open.er-api.com is the primary
 * (about 160 currencies including AED/SAR; free, daily, needs attribution).
 * Frankfurter (ECB reference rates) is the fallback: no key, but only ~30
 * major currencies. Kept behind this one class so a paid provider can replace
 * them without touching callers.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExchangeRateProviderClient {

    public record FetchedRates(String source, Map<String, BigDecimal> ratesPerUsd) {}

    private static final String PRIMARY_URL = "https://open.er-api.com/v6/latest/USD";
    private static final String FALLBACK_URL = "https://api.frankfurter.dev/v1/latest?base=USD";

    private final RestClient restClient;

    /** Returns rates per 1 USD (USD itself included as 1), or null if every provider failed. */
    public FetchedRates fetch() {
        FetchedRates primary = tryFetch("open.er-api.com", PRIMARY_URL);
        if (primary != null) return primary;
        return tryFetch("frankfurter.app", FALLBACK_URL);
    }

    @SuppressWarnings("unchecked")
    private FetchedRates tryFetch(String source, String url) {
        try {
            Map<String, Object> body = restClient.get().uri(url).retrieve().body(Map.class);
            if (body == null || !(body.get("rates") instanceof Map<?, ?> raw) || raw.isEmpty()) {
                log.warn("Exchange-rate provider {} returned no rates", source);
                return null;
            }
            Map<String, BigDecimal> rates = new HashMap<>();
            for (Map.Entry<?, ?> e : raw.entrySet()) {
                if (e.getValue() instanceof Number n) {
                    rates.put(String.valueOf(e.getKey()).toUpperCase(), new BigDecimal(n.toString()));
                }
            }
            rates.put("USD", BigDecimal.ONE);
            return new FetchedRates(source, rates);
        } catch (Exception e) {
            log.warn("Exchange-rate provider {} failed: {}", source, e.getMessage());
            return null;
        }
    }
}
