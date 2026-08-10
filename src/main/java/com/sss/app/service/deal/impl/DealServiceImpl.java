package com.sss.app.service.deal.impl;

import com.sss.app.dto.deal.DealResponseDTO;
import com.sss.app.helper.deal.DealHelper;
import com.sss.app.mapper.deal.DealMapper;
import com.sss.app.service.deal.DealService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DealServiceImpl implements DealService {

    private final DealHelper dealHelper;
    private final DealMapper dealMapper;

    @Override
    public DealResponseDTO acceptQuote(UUID quoteUid) {
        return dealMapper.toResponse(dealHelper.acceptQuote(quoteUid));
    }

    @Override
    public DealResponseDTO getByUid(UUID uid) {
        return dealMapper.toResponse(dealHelper.getByUid(uid));
    }

    @Override
    public DealResponseDTO getForEscape(UUID escapeUid) {
        return dealMapper.toResponse(dealHelper.getForEscape(escapeUid));
    }
}
