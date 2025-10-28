package com.sss.app.service.library.inclusionexclusion;

import com.sss.app.dto.library.inclusionexclusion.InclusionExclusionCreateRequestDto;
import com.sss.app.dto.library.inclusionexclusion.InclusionExclusionResponseDto;
import com.sss.app.dto.library.inclusionexclusion.InclusionExclusionUpdateRequestDto;

import java.util.List;

public interface InclusionExclusionsService {
    List<InclusionExclusionResponseDto> fetchAllInclusionExclusions(Long companyId);

    List<InclusionExclusionResponseDto> fetchAllInclusions(Long companyId);

    List<InclusionExclusionResponseDto> fetchAllExclusions(Long companyId);

    InclusionExclusionResponseDto getInclusionExclusionByUid(String uid);

    InclusionExclusionResponseDto createInclusionExclusion(InclusionExclusionCreateRequestDto payload);

    InclusionExclusionResponseDto updateInclusionExclusion(String uid, InclusionExclusionUpdateRequestDto payload);
}
