package com.sss.app.service.library.escapepoint;

import com.sss.app.dto.library.escapepoint.EscapePointCreateRequestDto;
import com.sss.app.dto.library.escapepoint.EscapePointResponseDto;
import com.sss.app.dto.library.escapepoint.EscapePointUpdateRequestDto;

import java.util.List;

public interface EscapePointsService {
    List<EscapePointResponseDto> fetchAllEscapePoints(Long companyId);

    EscapePointResponseDto getEscapePointByUid(String uid);

    EscapePointResponseDto createEscapePoint(EscapePointCreateRequestDto payload);

    EscapePointResponseDto updateEscapePoint(String uid, EscapePointUpdateRequestDto payload);

}
