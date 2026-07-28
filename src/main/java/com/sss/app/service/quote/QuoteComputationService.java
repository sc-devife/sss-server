package com.sss.app.service.quote;

import com.sss.app.dto.quote.QuoteComputeRequestDTO;
import com.sss.app.dto.quote.QuoteComputeResponseDTO;

import java.util.UUID;

public interface QuoteComputationService {
    QuoteComputeResponseDTO compute(UUID quoteUid, QuoteComputeRequestDTO request);
}
