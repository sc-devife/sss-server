package com.sss.app.service.quote.impl;

import com.sss.app.dto.quote.QuoteCreateRequestDTO;
import com.sss.app.dto.quote.QuoteResponseDTO;
import com.sss.app.dto.quote.QuoteUpdateRequestDTO;
import com.sss.app.entity.quote.Quote;
import com.sss.app.helper.quote.QuoteHelper;
import com.sss.app.mapper.quote.QuoteResponseAssembler;
import com.sss.app.service.quote.QuoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuoteServiceImpl implements QuoteService {

    private final QuoteHelper quoteHelper;
    private final QuoteResponseAssembler quoteResponseAssembler;

    @Override
    public QuoteResponseDTO create(QuoteCreateRequestDTO request) {
        return toResponse(quoteHelper.create(request));
    }

    @Override
    public QuoteResponseDTO getByUid(UUID uid) {
        return toResponse(quoteHelper.getByUid(uid));
    }

    @Override
    public List<QuoteResponseDTO> getAllForItinerary(UUID itineraryUid) {
        return quoteHelper.getAllForItinerary(itineraryUid).stream().map(this::toResponse).toList();
    }

    @Override
    public QuoteResponseDTO update(UUID uid, QuoteUpdateRequestDTO request) {
        return toResponse(quoteHelper.update(uid, request));
    }

    @Override
    public void delete(UUID uid) {
        quoteHelper.delete(uid);
    }

    @Override
    public QuoteResponseDTO markSent(UUID uid) {
        return toResponse(quoteHelper.markSent(uid));
    }

    @Override
    public QuoteResponseDTO markRejected(UUID uid) {
        return toResponse(quoteHelper.markRejected(uid));
    }

    private QuoteResponseDTO toResponse(Quote entity) {
        return quoteResponseAssembler.toResponse(entity);
    }
}
