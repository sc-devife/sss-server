package com.sss.app.service.escape.impl;

import com.sss.app.dto.escape.EscapeCreateRequestDTO;
import com.sss.app.dto.escape.EscapeResponseDTO;
import com.sss.app.dto.escape.EscapeUpdateRequestDTO;
import com.sss.app.helper.escape.EscapeHelper;
import com.sss.app.mapper.escape.EscapeMapper;
import com.sss.app.service.escape.EscapeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EscapeServiceImpl implements EscapeService {

    private final EscapeMapper escapeMapper;
    private final EscapeHelper escapeHelper;

    @Override
    public EscapeResponseDTO createEscape(EscapeCreateRequestDTO request) {

        return escapeMapper.toResponse(escapeHelper.createEscape(request));
    }

    @Override
    public EscapeResponseDTO updateEscape(Long seqp, EscapeUpdateRequestDTO request)
    {
        return escapeMapper.toResponse(escapeHelper.updateEscape(seqp, request));
    }


    @Override
    public EscapeResponseDTO getEscapeById(Long id) {
        return escapeMapper.toResponse(escapeHelper.getEscapeById(id));
    }

    @Override
    public List<EscapeResponseDTO> getAllEscapes() {
        return escapeHelper.getAllEscapes().stream().map(escapeMapper::toResponse).toList();
    }

    @Override
    public void deleteEscape(Long seqp) {
        escapeHelper.deleteEscape(seqp);
    }

}
