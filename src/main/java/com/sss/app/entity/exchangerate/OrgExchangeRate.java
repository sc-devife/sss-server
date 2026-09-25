package com.sss.app.entity.exchangerate;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** A vendor's own "1 <from> = rate <to>" - overrides the market rate while manual (see V131). */
@Entity
@Table(name = "org_exchange_rates")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrgExchangeRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seqp;

    @Column(name = "org_id", nullable = false)
    private Long orgId;

    @Column(name = "from_currency", nullable = false)
    private String fromCurrency;

    @Column(name = "to_currency", nullable = false)
    private String toCurrency;

    @Column(nullable = false, precision = 24, scale = 10)
    private BigDecimal rate;

    @Column(name = "is_manual", nullable = false)
    private Boolean manual;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Long updatedBy;
}
