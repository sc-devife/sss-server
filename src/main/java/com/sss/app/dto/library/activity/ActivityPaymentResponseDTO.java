package com.sss.app.dto.library.activity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityPaymentResponseDTO {

    private UUID uid;

    private UUID escapeUid;

    private String tripCode;

    private String transactionId;

    private String paymentMethod;

    private BigDecimal amount;

    private String paidBy;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate paymentDate;

    private String notes;

    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    // Present only for a payment made in a non-base currency.
    private BigDecimal paidAmount;

    private String paidCurrency;

    private BigDecimal fxRate;
}
