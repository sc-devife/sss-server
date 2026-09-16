package com.sss.app.dto.transaction;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

// One row on the Accounting > Transactions > Outgoing ledger — a payout the
// agency made to a hotel (see HotelPayment), enriched with who it went to
// and which trip it belongs to, mirroring IncomingTransactionResponseDTO's
// shape for the customer-payment side.
@Data
public class OutgoingTransactionResponseDTO {
    private UUID paymentUid;
    // "Hotel" or "Activity" — which vendor type this payout went to, so the
    // frontend knows whether vendorUid resolves via /library/hotels/{uid}
    // or /library/activities/{uid}.
    private String vendorType;
    private UUID vendorUid;
    private String vendorName;
    private UUID escapeUid;
    private String tripCode;
    private String transactionId;
    private String paymentMethod;
    private BigDecimal amount;
    private String paidBy;
    private LocalDate paymentDate;
    private String notes;
    private String status;
    private LocalDateTime createdAt;
}
