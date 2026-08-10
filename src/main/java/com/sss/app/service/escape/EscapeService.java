package com.sss.app.service.escape;

import com.sss.app.dto.escape.EscapeCreateRequestDTO;
import com.sss.app.dto.escape.EscapeResponseDTO;
import com.sss.app.dto.escape.EscapeUpdateRequestDTO;

import java.util.List;
import java.util.UUID;

public interface EscapeService {
    EscapeResponseDTO createEscape(EscapeCreateRequestDTO request);
    EscapeResponseDTO updateEscape(UUID uid, EscapeUpdateRequestDTO request);
    EscapeResponseDTO getEscapeById(UUID id);
    List<EscapeResponseDTO> getAllEscapes();
    void deleteEscape(UUID id);

    // Internal-only: resolves the external uid to the entity's internal
    // seqp, for callers (e.g. audit log lookups) that must keep using the
    // Long-keyed AuditLog storage without leaking seqp through the response DTO.
    Long resolveSeqp(UUID id);
}
