package com.sss.app.dto.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentRecordRequestDTO {
    @NotNull(message = "amount is required")
    private BigDecimal amount;

    @NotBlank(message = "paymentMethod is required")
    private String paymentMethod;

    @NotBlank(message = "paymentReference is required")
    private String paymentReference;
}
