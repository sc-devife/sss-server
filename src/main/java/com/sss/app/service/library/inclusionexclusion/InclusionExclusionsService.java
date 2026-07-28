package com.sss.app.service.library.inclusionexclusion;

import com.sss.app.dto.library.inclusionexclusion.InclusionExclusionCreateRequestDto;
import com.sss.app.dto.library.inclusionexclusion.InclusionExclusionResponseDto;
import com.sss.app.dto.library.inclusionexclusion.InclusionExclusionUpdateRequestDto;

import java.util.List;
import java.util.UUID;

public interface InclusionExclusionsService {
    List<InclusionExclusionResponseDto> fetchAllForOrg(String type);

    InclusionExclusionResponseDto getByUid(String uid);

    InclusionExclusionResponseDto create(InclusionExclusionCreateRequestDto payload);

    InclusionExclusionResponseDto update(String uid, InclusionExclusionUpdateRequestDto payload);

    InclusionExclusionResponseDto deactivate(String uid);

    List<InclusionExclusionResponseDto> getSelectableForItinerary(UUID itineraryUid, String type);
}
