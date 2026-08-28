package com.sss.app.service.library.escapepoint.impl;

import com.sss.app.dto.library.escapepoint.EscapePointCreateRequestDto;
import com.sss.app.dto.library.escapepoint.EscapePointLocationsUpdateRequestDto;
import com.sss.app.dto.library.escapepoint.EscapePointResponseDto;
import com.sss.app.dto.library.escapepoint.EscapePointUpdateRequestDto;
import com.sss.app.entity.library.escapepoint.EscapePoint;
import com.sss.app.helper.library.escapepoint.EscapePointsHelper;
import com.sss.app.mapper.library.escapepoint.EscapePointMapper;
import com.sss.app.service.library.escapepoint.EscapePointLocationResolver;
import com.sss.app.service.library.escapepoint.EscapePointsService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EscapePointsServiceImpl implements EscapePointsService {

    private final EscapePointsHelper escapePointsHelper;
    private final EscapePointMapper escapePointMapper;
    private final EscapePointLocationResolver escapePointLocationResolver;

    public EscapePointsServiceImpl(EscapePointsHelper escapePointsHelper, EscapePointMapper escapePointMapper,
                                    EscapePointLocationResolver escapePointLocationResolver) {
        this.escapePointsHelper = escapePointsHelper;
        this.escapePointMapper = escapePointMapper;
        this.escapePointLocationResolver = escapePointLocationResolver;
    }

    @Override
    public List<EscapePointResponseDto> fetchAllEscapePoints() {
        List<EscapePoint> entities = escapePointsHelper.fetchAllEscapePoints();
        return enrich(entities, escapePointMapper.toDtoList(entities));
    }

    @Override
    public EscapePointResponseDto getEscapePointByUid(String uid) {
        EscapePoint entity = escapePointsHelper.getEscapePointByUid(uid);
        return enrich(List.of(entity), List.of(escapePointMapper.toDto(entity))).get(0);
    }

    @Override
    public EscapePointResponseDto createEscapePoint(EscapePointCreateRequestDto payload) {
        EscapePoint entity = escapePointsHelper.createEscapePoint(payload);
        return enrich(List.of(entity), List.of(escapePointMapper.toDto(entity))).get(0);
    }

    @Override
    public EscapePointResponseDto updateEscapePoint(String uid, EscapePointUpdateRequestDto payload) {
        EscapePoint entity = escapePointsHelper.updateEscapePoint(uid, payload);
        return enrich(List.of(entity), List.of(escapePointMapper.toDto(entity))).get(0);
    }

    @Override
    public void deleteEscapePoint(String uid) {
        escapePointsHelper.deleteEscapePoint(uid);
    }

    @Override
    public EscapePointResponseDto updateLocations(String uid, EscapePointLocationsUpdateRequestDto payload) {
        EscapePoint entity = escapePointsHelper.reassignLocations(uid, payload);
        return enrich(List.of(entity), List.of(escapePointMapper.toDto(entity))).get(0);
    }

    private List<EscapePointResponseDto> enrich(List<EscapePoint> entities, List<EscapePointResponseDto> dtos) {
        escapePointLocationResolver.resolve(entities.stream().map(EscapePoint::getSeqp).toList(), dtos);
        return dtos;
    }
}
