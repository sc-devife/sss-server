package com.sss.app.dto.library.activity;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class ActivityPaymentCreateRequestDTO {

    @NotNull(message = "Escape is required")
    private UUID escapeUid;

    private String transactionId;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    // Optional: the currency the supplier was actually paid in (amount above is in it). Blank = base currency.
    private String currencyCode;

    // Optional: "1 base = exchangeRate <currencyCode>"; blank = today's rate.
    private java.math.BigDecimal exchangeRate;

    private String paidBy;

    @NotNull(message = "Payment date is required")
    private LocalDate paymentDate;

    private String notes;
}
