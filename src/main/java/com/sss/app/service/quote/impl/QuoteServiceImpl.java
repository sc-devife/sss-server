package com.sss.app.service.quote.impl;

import com.sss.app.dto.quote.QuoteCreateRequestDTO;
import com.sss.app.dto.quote.QuoteResponseDTO;
import com.sss.app.dto.quote.QuoteUpdateRequestDTO;
import com.sss.app.entity.quote.Quote;
import com.sss.app.entity.users.User;
import com.sss.app.helper.quote.QuoteHelper;
import com.sss.app.mapper.quote.QuoteMapper;
import com.sss.app.repository.UserRepository;
import com.sss.app.service.quote.QuoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuoteServiceImpl implements QuoteService {

    private final QuoteHelper quoteHelper;
    private final QuoteMapper quoteMapper;
    private final UserRepository userRepository;

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
    public QuoteResponseDTO createRevision(UUID sourceUid) {
        return toResponse(quoteHelper.createRevision(sourceUid));
    }

    @Override
    public QuoteResponseDTO markSent(UUID uid) {
        return toResponse(quoteHelper.markSent(uid));
    }

    @Override
    public QuoteResponseDTO markRejected(UUID uid) {
        return toResponse(quoteHelper.markRejected(uid));
    }

    // Wraps the MapStruct mapping to resolve createdBy -> a display name —
    // MapStruct can't do that lookup declaratively, so it's done here once
    // rather than at every call site.
    private QuoteResponseDTO toResponse(Quote entity) {
        QuoteResponseDTO dto = quoteMapper.toResponse(entity);
        if (entity.getCreatedBy() != null) {
            userRepository.findById(entity.getCreatedBy()).map(User::getName).ifPresent(dto::setCreatedByName);
        }
        return dto;
    }
}
