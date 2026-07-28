package com.sss.app.service.quote;

import com.sss.app.dto.quote.QuoteCreateRequestDTO;
import com.sss.app.dto.quote.QuoteResponseDTO;
import com.sss.app.dto.quote.QuoteUpdateRequestDTO;

import java.util.List;
import java.util.UUID;

public interface QuoteService {
    QuoteResponseDTO create(QuoteCreateRequestDTO request);
    QuoteResponseDTO getByUid(UUID uid);
    List<QuoteResponseDTO> getAllForItinerary(UUID itineraryUid);
    QuoteResponseDTO update(UUID uid, QuoteUpdateRequestDTO request);
    void delete(UUID uid);
    QuoteResponseDTO createRevision(UUID sourceUid);
}
