package com.sss.app.service.library.transport;

import com.sss.app.dto.library.transport.TransportCreateRequestDTO;
import com.sss.app.dto.library.transport.TransportResponseDTO;
import com.sss.app.dto.library.transport.TransportUpdateRequestDTO;

import java.util.List;
import java.util.UUID;

public interface TransportService {
    TransportResponseDTO create(TransportCreateRequestDTO dto);

    TransportResponseDTO getById(UUID id);

    List<TransportResponseDTO> getAll();

    TransportResponseDTO update(UUID id, TransportUpdateRequestDTO dto);

    void delete(UUID id);
}
