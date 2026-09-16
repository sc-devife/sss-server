package com.sss.app.dto.library.hotel;

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
public class HotelPaymentResponseDTO {

    private UUID uid;

    private UUID escapeUid;

    // Human-readable trip code (e.g. "TRP-000123") — see Escape.tripCode.
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
}
