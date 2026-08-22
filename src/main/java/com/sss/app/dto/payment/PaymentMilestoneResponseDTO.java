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
    private BigDecimal amountUsd;
    private BigDecimal amountPaidUsd;
    private String status;
    private Long markedPaidBy;
    private String markedPaidByName;
    private LocalDateTime markedPaidAt;
}
