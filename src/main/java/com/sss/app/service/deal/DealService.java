package com.sss.app.service.deal;

import com.sss.app.dto.deal.DealResponseDTO;

import java.util.UUID;

public interface DealService {
    DealResponseDTO acceptQuote(UUID quoteUid);
    DealResponseDTO getByUid(UUID uid);
    DealResponseDTO getForEscape(Long escapeId);
}
