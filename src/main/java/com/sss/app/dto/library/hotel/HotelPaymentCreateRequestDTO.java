package com.sss.app.dto.library.hotel;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class HotelPaymentCreateRequestDTO {

    @NotNull(message = "Escape is required")
    private UUID escapeUid;

    private String transactionId;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    private String paidBy;

    @NotNull(message = "Payment date is required")
    private LocalDate paymentDate;

    private String notes;
}
