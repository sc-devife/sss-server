package com.sss.app.dto.payment;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class PaymentRecordResponseDTO {
    private UUID uid;
    private BigDecimal receivedAmount;
    private String receivedCurrency;
    /** "1 base = fxRate <receivedCurrency>" at receipt (1 for a payment in the base currency). */
    private BigDecimal fxRate;
    /** Credited against the milestone, in base currency. */
    private BigDecimal appliedAmountBase;
    /** What the money is worth in base at the receipt rate. */
    private BigDecimal baseValueReceived;
    /** baseValueReceived - appliedAmountBase: positive = FX gain, negative = FX loss. */
    private BigDecimal fxDifferenceBase;
    private String paymentMethod;
    private String paymentReference;
    private String recordedByName;
    private LocalDateTime recordedAt;
    /** Null while the payment is still awaiting verification. */
    private LocalDateTime verifiedAt;
}
