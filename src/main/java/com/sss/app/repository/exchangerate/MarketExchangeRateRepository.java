package com.sss.app.repository.exchangerate;

import com.sss.app.entity.exchangerate.MarketExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface MarketExchangeRateRepository extends JpaRepository<MarketExchangeRate, Long> {

    Optional<MarketExchangeRate> findTopByCurrencyCodeOrderByAsOfDateDesc(String currencyCode);

    Optional<MarketExchangeRate> findByCurrencyCodeAndAsOfDate(String currencyCode, LocalDate asOfDate);

    boolean existsByAsOfDate(LocalDate asOfDate);
}
