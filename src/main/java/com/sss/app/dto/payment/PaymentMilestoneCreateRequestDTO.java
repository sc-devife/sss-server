package com.sss.app.dto.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class PaymentMilestoneCreateRequestDTO {

    @NotNull(message = "dealUid is required")
    private UUID dealUid;

    @NotBlank(message = "Label is required")
    private String label;

    @NotNull(message = "dueDate is required")
    private LocalDate dueDate;

    @NotNull(message = "amountInr is required")
    private BigDecimal amountInr;
}
