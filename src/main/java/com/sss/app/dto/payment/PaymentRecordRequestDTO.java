package com.sss.app.dto.payment;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentRecordRequestDTO {
    @NotNull(message = "amount is required")
    private BigDecimal amount;
}
