package com.sss.app.dto.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentRecordRequestDTO {
    @NotNull(message = "amount is required")
    private BigDecimal amount;

    // Optional: the currency the money actually arrived in. Blank = the vendor's base currency.
    private String currencyCode;

    // Optional: "1 base = exchangeRate <currencyCode>". Blank = today's rate (the vendor's manual rate, else market).
    private BigDecimal exchangeRate;

    @NotBlank(message = "paymentMethod is required")
    private String paymentMethod;

    @NotBlank(message = "paymentReference is required")
    private String paymentReference;
}
