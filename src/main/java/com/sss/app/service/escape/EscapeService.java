package com.sss.app.service.escape;

import com.sss.app.dto.escape.EscapeCreateRequestDTO;
import com.sss.app.dto.escape.EscapeResponseDTO;
import com.sss.app.dto.escape.EscapeUpdateRequestDTO;

import java.util.List;

public interface EscapeService {
    EscapeResponseDTO createEscape(EscapeCreateRequestDTO request);
    EscapeResponseDTO updateEscape(Long seqp, EscapeUpdateRequestDTO request);
    EscapeResponseDTO getEscapeById(Long id);
    List<EscapeResponseDTO> getAllEscapes();
    void deleteEscape(Long id);
}
