package com.sss.app.entity.exchangerate;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** One day's market rate for a currency, expressed as units of it per 1 USD (see V131). */
@Entity
@Table(name = "market_exchange_rates")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketExchangeRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seqp;

    @Column(name = "currency_code", nullable = false)
    private String currencyCode;

    @Column(name = "rate_per_usd", nullable = false, precision = 24, scale = 10)
    private BigDecimal ratePerUsd;

    @Column(name = "as_of_date", nullable = false)
    private LocalDate asOfDate;

    @Column(nullable = false)
    private String source;

    @Column(name = "fetched_at", nullable = false)
    private LocalDateTime fetchedAt;
}
