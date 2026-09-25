package com.sss.app.dto.transaction;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

// One row on the Accounting > Transactions > Incoming ledger — a payment
// milestone that's actually had money recorded against it, enriched with
// who it came from (customer/lead) and which trip it belongs to, so the
// list doesn't need a second lookup per row.
@Data
public class IncomingTransactionResponseDTO {
    private UUID milestoneUid;
    private UUID dealUid;
    private UUID escapeUid;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String label;
    private BigDecimal amountBase;
    private BigDecimal amountPaidBase;
    private String status;
    // Every payment received against this milestone (with its currency/rate) and the net FX gain/loss.
    private java.util.List<com.sss.app.dto.payment.PaymentRecordResponseDTO> payments;
    private BigDecimal fxDifferenceBase;
    private String paymentMethod;
    private String paymentReference;
    private LocalDateTime markedPaidAt;
    private String markedPaidByName;
}
