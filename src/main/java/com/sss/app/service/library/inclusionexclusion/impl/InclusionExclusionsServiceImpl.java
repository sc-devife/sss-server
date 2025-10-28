package com.sss.app.service.library.inclusionexclusion.impl;

import com.sss.app.dto.library.inclusionexclusion.InclusionExclusionCreateRequestDto;
import com.sss.app.dto.library.inclusionexclusion.InclusionExclusionResponseDto;
import com.sss.app.dto.library.inclusionexclusion.InclusionExclusionUpdateRequestDto;
import com.sss.app.helper.library.inclusionexclusion.InclusionExclusionsHelper;
import com.sss.app.mapper.library.inclusionexclusion.InclusionExclusionMapper;
import com.sss.app.service.library.inclusionexclusion.InclusionExclusionsService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InclusionExclusionsServiceImpl implements InclusionExclusionsService {

    private final InclusionExclusionsHelper inclusionExclusionsHelper;
    private final InclusionExclusionMapper inclusionExclusionMapper;

    public InclusionExclusionsServiceImpl(InclusionExclusionsHelper inclusionExclusionsHelper, InclusionExclusionMapper inclusionExclusionMapper) {
        this.inclusionExclusionsHelper = inclusionExclusionsHelper;
        this.inclusionExclusionMapper = inclusionExclusionMapper;
    }

    @Override
    public List<InclusionExclusionResponseDto> fetchAllInclusionExclusions(Long companyId) {
        return inclusionExclusionMapper.toDtoList(inclusionExclusionsHelper.fetchAllInclusionExclusions(companyId));
    }

    @Override
    public List<InclusionExclusionResponseDto> fetchAllInclusions(Long companyId) {
        return inclusionExclusionMapper.toDtoList(inclusionExclusionsHelper.fetchAllInclusions(companyId));
    }

    @Override
    public List<InclusionExclusionResponseDto> fetchAllExclusions(Long companyId) {
        return inclusionExclusionMapper.toDtoList(inclusionExclusionsHelper.fetchAllExclusions(companyId));
    }

    @Override
    public InclusionExclusionResponseDto getInclusionExclusionByUid(String uid) {
        return inclusionExclusionMapper.toDto(inclusionExclusionsHelper.getInclusionExclusionByUid(uid));
    }

    @Override
    public InclusionExclusionResponseDto createInclusionExclusion(InclusionExclusionCreateRequestDto payload) {
        return inclusionExclusionMapper.toDto(inclusionExclusionsHelper.createInclusionExclusion(payload));
    }

    @Override
    public InclusionExclusionResponseDto updateInclusionExclusion(String uid, InclusionExclusionUpdateRequestDto payload) {
        return inclusionExclusionMapper.toDto(inclusionExclusionsHelper.updateInclusionExclusion(uid, payload));
    }
}
