package com.sss.app.dto.deal;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class DealResponseDTO {
    private UUID uid;
    private Long tripId;
    private UUID acceptedQuoteUid;
    private String status;
    private LocalDateTime createdAt;
}
