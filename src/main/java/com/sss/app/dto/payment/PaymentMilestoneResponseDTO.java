package com.sss.app.dto.payment;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class PaymentMilestoneResponseDTO {
    private UUID uid;
    private UUID dealUid;
    private String label;
    private LocalDate dueDate;
    private BigDecimal amountBase;
    private BigDecimal amountPaidBase;
    private String status;
    private Long markedPaidBy;
    private String markedPaidByName;
    private LocalDateTime markedPaidAt;
    private String paymentMethod;
    private String paymentReference;
    // Each payment received, in the currency it arrived in, and the net FX gain/loss across them (base currency).
    private java.util.List<PaymentRecordResponseDTO> payments;
    private BigDecimal fxDifferenceBase;
}
