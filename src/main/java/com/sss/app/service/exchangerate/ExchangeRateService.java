package com.sss.app.service.exchangerate;

import com.sss.app.dto.exchangerate.ExchangeRateResponseDTO;
import com.sss.app.dto.exchangerate.OrgExchangeRateRowDTO;
import com.sss.app.dto.exchangerate.OrgExchangeRateUpdateRequestDTO;

import java.util.List;

/**
 * Exchange rates for multi-currency quoting. A pair resolves in this order:
 * the vendor's manual rate (if switched on), otherwise the latest market rate
 * (derived through USD from the daily refresh). Rates always read
 * "1 <from> = rate <to>" with the vendor's own currency first.
 */
public interface ExchangeRateService {

    /** The rate that applies to from -> to for the caller's vendor. */
    ExchangeRateResponseDTO resolve(String from, String to);

    /** The caller's base currency against every other supported currency, with market and manual rates. */
    List<OrgExchangeRateRowDTO> listForCurrentOrg();

    /** Sets (or switches on/off) the vendor's own rate for base -> to. */
    OrgExchangeRateRowDTO setOrgRate(String to, OrgExchangeRateUpdateRequestDTO request);

    /** Removes the vendor's own rate so the pair follows the market again. */
    OrgExchangeRateRowDTO resetOrgRate(String to);

    /** Fetches today's market rates now. Returns how many currencies were stored (0 if every provider failed). */
    int refreshMarketRates();
}
