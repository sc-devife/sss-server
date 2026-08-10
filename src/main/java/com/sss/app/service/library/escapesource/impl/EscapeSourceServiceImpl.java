package com.sss.app.service.library.escapesource.impl;

import com.sss.app.dto.library.escapesource.EscapeSourceRequestDTO;
import com.sss.app.dto.library.escapesource.EscapeSourceResponseDTO;
import com.sss.app.helper.library.escapesource.EscapeSourceHelper;
import com.sss.app.mapper.library.escapesource.EscapeSourceMapper;
import com.sss.app.service.library.escapesource.EscapeSourceService;
//import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

//import java.awt.print.Pageable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import java.util.List;
import java.util.UUID;

@Service
public class EscapeSourceServiceImpl implements EscapeSourceService {
    //private final EscapeSourceRepository repository;

    private final EscapeSourceHelper escapeSourceHelper;
    private final EscapeSourceMapper escapeSourceMapper;

    public EscapeSourceServiceImpl(EscapeSourceHelper escapeSourceHelper, EscapeSourceMapper escapeSourceMapper) {
        this.escapeSourceHelper = escapeSourceHelper;
        this.escapeSourceMapper = escapeSourceMapper;
    }

    @Override
    public EscapeSourceResponseDTO createEscapeSource(EscapeSourceRequestDTO payload) {
        return escapeSourceMapper.toDto(escapeSourceHelper.createEscapeSource(payload));
    }
    @Override
    public EscapeSourceResponseDTO updateEscapeSource(UUID id, EscapeSourceRequestDTO payload) {
        return escapeSourceMapper.toDto(
                escapeSourceHelper.updateEscapeSource(id, payload)
        );
    }
    @Override
    public EscapeSourceResponseDTO getSourceById(UUID id) {
        return escapeSourceMapper.toDto(
                escapeSourceHelper.getSourceById(id)
        );
    }

    @Override
    public List<EscapeSourceResponseDTO> getAllSources() {
        return escapeSourceHelper.getAllSources()
                .stream()
                .map(escapeSourceMapper::toDto)
                .toList();
    }
    @Override
    public Page<EscapeSourceResponseDTO> getSources(Pageable pageable) {

        return escapeSourceHelper.getSources(pageable)
                .map(escapeSourceMapper::toDto);
    }
}
