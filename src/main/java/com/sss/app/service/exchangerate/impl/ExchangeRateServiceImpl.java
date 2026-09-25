package com.sss.app.service.exchangerate.impl;

import com.sss.app.dto.exchangerate.ExchangeRateResponseDTO;
import com.sss.app.dto.exchangerate.OrgExchangeRateRowDTO;
import com.sss.app.dto.exchangerate.OrgExchangeRateUpdateRequestDTO;
import com.sss.app.entity.exchangerate.MarketExchangeRate;
import com.sss.app.entity.exchangerate.OrgExchangeRate;
import com.sss.app.entity.system.Currency;
import com.sss.app.entity.users.User;
import com.sss.app.exception.BadRequestException;
import com.sss.app.repository.OrganizationSettingsRepository;
import com.sss.app.repository.exchangerate.MarketExchangeRateRepository;
import com.sss.app.repository.exchangerate.OrgExchangeRateRepository;
import com.sss.app.repository.system.CurrencyRepository;
import com.sss.app.service.exchangerate.ExchangeRateProviderClient;
import com.sss.app.service.exchangerate.ExchangeRateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeRateServiceImpl implements ExchangeRateService {

    private static final String DEFAULT_BASE = "INR";
    private static final int SCALE = 8;

    private final MarketExchangeRateRepository marketRepository;
    private final OrgExchangeRateRepository orgRepository;
    private final OrganizationSettingsRepository settingsRepository;
    private final CurrencyRepository currencyRepository;
    private final ExchangeRateProviderClient providerClient;

    private User currentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private String baseCurrency(Long orgId) {
        return settingsRepository.findById(orgId)
                .map(s -> s.getDefaultCurrencyCode())
                .filter(c -> c != null && !c.isBlank())
                .orElse(DEFAULT_BASE);
    }

    // ---- market rates -------------------------------------------------

    /** Latest market rate for 1 from = ? to, plus its date; empty when either currency has no rate yet. */
    private Optional<MarketQuote> market(String from, String to) {
        if (from.equals(to)) return Optional.of(new MarketQuote(BigDecimal.ONE, LocalDate.now()));
        Optional<MarketExchangeRate> f = marketRepository.findTopByCurrencyCodeOrderByAsOfDateDesc(from);
        Optional<MarketExchangeRate> t = marketRepository.findTopByCurrencyCodeOrderByAsOfDateDesc(to);
        if (f.isEmpty() || t.isEmpty()) return Optional.empty();
        BigDecimal rate = t.get().getRatePerUsd().divide(f.get().getRatePerUsd(), SCALE + 4, RoundingMode.HALF_UP);
        LocalDate asOf = f.get().getAsOfDate().isBefore(t.get().getAsOfDate()) ? f.get().getAsOfDate() : t.get().getAsOfDate();
        return Optional.of(new MarketQuote(rate.round(new MathContext(12)), asOf));
    }

    private record MarketQuote(BigDecimal rate, LocalDate asOf) {}

    @Override
    @Transactional
    public int refreshMarketRates() {
        ExchangeRateProviderClient.FetchedRates fetched = providerClient.fetch();
        if (fetched == null) {
            log.warn("Market exchange rates not refreshed - every provider failed; keeping the last known rates");
            return 0;
        }
        LocalDate today = LocalDate.now();
        List<String> wanted = new ArrayList<>();
        wanted.add("USD");
        for (Currency c : currencyRepository.findAll()) {
            if (c.getCode() != null && !"USD".equalsIgnoreCase(c.getCode())) wanted.add(c.getCode().toUpperCase());
        }
        int stored = 0;
        for (String code : wanted) {
            BigDecimal rate = fetched.ratesPerUsd().get(code);
            if (rate == null || rate.signum() <= 0) continue;
            MarketExchangeRate row = marketRepository.findByCurrencyCodeAndAsOfDate(code, today)
                    .orElseGet(() -> MarketExchangeRate.builder().currencyCode(code).asOfDate(today).build());
            row.setRatePerUsd(rate);
            row.setSource(fetched.source());
            row.setFetchedAt(LocalDateTime.now());
            marketRepository.save(row);
            stored++;
        }
        log.info("Stored {} market exchange rates from {} for {}", stored, fetched.source(), today);
        return stored;
    }

    // Daily refresh, plus one on startup when today's rates aren't there yet
    // (first run, or the server was down at the scheduled time).
    @Scheduled(cron = "0 30 1 * * *")
    public void scheduledRefresh() {
        refreshMarketRates();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void refreshOnStartupIfMissing() {
        try {
            if (!marketRepository.existsByAsOfDate(LocalDate.now())) refreshMarketRates();
        } catch (Exception e) {
            log.warn("Startup exchange-rate refresh failed: {}", e.getMessage());
        }
    }

    // ---- resolution ---------------------------------------------------

    @Override
    public ExchangeRateResponseDTO resolve(String from, String to) {
        String f = from.toUpperCase();
        String t = to.toUpperCase();
        if (f.equals(t)) {
            return new ExchangeRateResponseDTO(f, t, BigDecimal.ONE, "same", false, LocalDate.now(), BigDecimal.ONE);
        }
        Optional<MarketQuote> mq = market(f, t);
        Optional<OrgExchangeRate> own = orgRepository.findByOrgIdAndFromCurrencyAndToCurrency(currentUser().getOrgId(), f, t);
        if (own.isPresent() && Boolean.TRUE.equals(own.get().getManual())) {
            return new ExchangeRateResponseDTO(f, t, own.get().getRate(), "manual", true,
                    mq.map(MarketQuote::asOf).orElse(null), mq.map(MarketQuote::rate).orElse(null));
        }
        MarketQuote q = mq.orElseThrow(() -> new BadRequestException("No exchange rate available for " + f + " to " + t + " yet"));
        return new ExchangeRateResponseDTO(f, t, q.rate(), "market", false, q.asOf(), q.rate());
    }

    // ---- vendor rate table -------------------------------------------

    private OrgExchangeRateRowDTO row(Long orgId, String base, Currency currency) {
        String code = currency.getCode().toUpperCase();
        Optional<MarketQuote> mq = market(base, code);
        Optional<OrgExchangeRate> own = orgRepository.findByOrgIdAndFromCurrencyAndToCurrency(orgId, base, code);
        boolean manual = own.isPresent() && Boolean.TRUE.equals(own.get().getManual());
        BigDecimal effective = manual ? own.get().getRate() : mq.map(MarketQuote::rate).orElse(null);
        return new OrgExchangeRateRowDTO(code, currency.getName(), mq.map(MarketQuote::rate).orElse(null),
                own.map(OrgExchangeRate::getRate).orElse(null), manual, effective, mq.map(MarketQuote::asOf).orElse(null));
    }

    @Override
    public List<OrgExchangeRateRowDTO> listForCurrentOrg() {
        Long orgId = currentUser().getOrgId();
        String base = baseCurrency(orgId);
        return currencyRepository.findAll().stream()
                .filter(c -> c.getCode() != null && !c.getCode().equalsIgnoreCase(base))
                .filter(c -> !Boolean.FALSE.equals(c.getIs_active()))
                .map(c -> row(orgId, base, c))
                .toList();
    }

    private Currency requireCurrency(String code) {
        return currencyRepository.findAll().stream()
                .filter(c -> c.getCode() != null && c.getCode().equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Unsupported currency: " + code));
    }

    @Override
    @Transactional
    public OrgExchangeRateRowDTO setOrgRate(String to, OrgExchangeRateUpdateRequestDTO request) {
        User user = currentUser();
        Long orgId = user.getOrgId();
        String base = baseCurrency(orgId);
        String code = to.toUpperCase();
        if (code.equals(base)) throw new BadRequestException("A rate needs two different currencies");
        Currency currency = requireCurrency(code);

        OrgExchangeRate row = orgRepository.findByOrgIdAndFromCurrencyAndToCurrency(orgId, base, code)
                .orElseGet(() -> OrgExchangeRate.builder().orgId(orgId).fromCurrency(base).toCurrency(code).build());
        row.setRate(request.getRate());
        row.setManual(request.getManual() == null || request.getManual());
        row.setUpdatedAt(LocalDateTime.now());
        row.setUpdatedBy(user.getSeqp());
        orgRepository.save(row);
        return row(orgId, base, currency);
    }

    @Override
    @Transactional
    public OrgExchangeRateRowDTO resetOrgRate(String to) {
        Long orgId = currentUser().getOrgId();
        String base = baseCurrency(orgId);
        String code = to.toUpperCase();
        orgRepository.findByOrgIdAndFromCurrencyAndToCurrency(orgId, base, code).ifPresent(orgRepository::delete);
        return row(orgId, base, requireCurrency(code));
    }
}
