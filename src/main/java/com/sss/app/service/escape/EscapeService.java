package com.sss.app.service.escape;

import com.sss.app.dto.escape.EscapeCreateRequestDTO;
import com.sss.app.dto.escape.EscapeResponseDTO;
import com.sss.app.dto.escape.EscapeUpdateRequestDTO;

public interface EscapeService {
    EscapeResponseDTO createEscape(EscapeCreateRequestDTO request);
    EscapeResponseDTO updateEscape(Long seqp, EscapeUpdateRequestDTO request);
    EscapeResponseDTO getTripById(Long id);
    void deleteEscape(Long id);
}
