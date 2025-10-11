package com.sss.app.service.library.escapepoint.impl;

import com.sss.app.dto.library.escapepoint.EscapePointCreateRequestDto;
import com.sss.app.dto.library.escapepoint.EscapePointResponseDto;
import com.sss.app.dto.library.escapepoint.EscapePointUpdateRequestDto;
import com.sss.app.helper.library.escapepoint.EscapePointsHelper;
import com.sss.app.mapper.library.escapepoint.EscapePointMapper;
import com.sss.app.service.library.escapepoint.EscapePointsService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EscapePointsServiceImpl implements EscapePointsService {

    private final EscapePointsHelper escapePointsHelper;
    private final EscapePointMapper escapePointMapper;

    public EscapePointsServiceImpl(EscapePointsHelper escapePointsHelper, EscapePointMapper escapePointMapper) {
        this.escapePointsHelper = escapePointsHelper;
        this.escapePointMapper = escapePointMapper;
    }

    @Override
    public List<EscapePointResponseDto> fetchAllEscapePoints(Long companyId) {
        return escapePointMapper.toDtoList(escapePointsHelper.fetchAllEscapePoints(companyId));
    }

    @Override
    public EscapePointResponseDto getEscapePointByUid(String uid) {
        return escapePointMapper.toDto(escapePointsHelper.getEscapePointByUid(uid));
    }

    @Override
    public EscapePointResponseDto createEscapePoint(EscapePointCreateRequestDto payload) {
        return escapePointMapper.toDto(escapePointsHelper.createEscapePoint(payload));
    }

    @Override
    public EscapePointResponseDto updateEscapePoint(String uid, EscapePointUpdateRequestDto payload) {
        return escapePointMapper.toDto(escapePointsHelper.updateEscapePoint(uid, payload));
    }
}
