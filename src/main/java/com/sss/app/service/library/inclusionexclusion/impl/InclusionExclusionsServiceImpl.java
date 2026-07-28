package com.sss.app.service.library.inclusionexclusion.impl;

import com.sss.app.dto.library.inclusionexclusion.InclusionExclusionCreateRequestDto;
import com.sss.app.dto.library.inclusionexclusion.InclusionExclusionResponseDto;
import com.sss.app.dto.library.inclusionexclusion.InclusionExclusionUpdateRequestDto;
import com.sss.app.helper.library.inclusionexclusion.InclusionExclusionsHelper;
import com.sss.app.mapper.library.inclusionexclusion.InclusionExclusionMapper;
import com.sss.app.service.library.inclusionexclusion.InclusionExclusionsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InclusionExclusionsServiceImpl implements InclusionExclusionsService {

    private final InclusionExclusionsHelper inclusionExclusionsHelper;
    private final InclusionExclusionMapper inclusionExclusionMapper;

    @Override
    public List<InclusionExclusionResponseDto> fetchAllForOrg(String type) {
        return inclusionExclusionMapper.toDtoList(inclusionExclusionsHelper.fetchAllForOrg(type));
    }

    @Override
    public InclusionExclusionResponseDto getByUid(String uid) {
        return inclusionExclusionMapper.toDto(inclusionExclusionsHelper.getByUid(uid));
    }

    @Override
    public InclusionExclusionResponseDto create(InclusionExclusionCreateRequestDto payload) {
        return inclusionExclusionMapper.toDto(inclusionExclusionsHelper.create(payload));
    }

    @Override
    public InclusionExclusionResponseDto update(String uid, InclusionExclusionUpdateRequestDto payload) {
        return inclusionExclusionMapper.toDto(inclusionExclusionsHelper.update(uid, payload));
    }

    @Override
    public InclusionExclusionResponseDto deactivate(String uid) {
        return inclusionExclusionMapper.toDto(inclusionExclusionsHelper.deactivate(uid));
    }

    @Override
    public List<InclusionExclusionResponseDto> getSelectableForItinerary(UUID itineraryUid, String type) {
        return inclusionExclusionMapper.toDtoList(inclusionExclusionsHelper.getSelectableForItinerary(itineraryUid, type));
    }
}
