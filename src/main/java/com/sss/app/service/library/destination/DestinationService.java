package com.sss.app.service.library.destination;

import com.sss.app.dto.library.destination.DestinationCreateRequestDTO;
import com.sss.app.dto.library.destination.DestinationResponseDTO;
import com.sss.app.dto.library.destination.DestinationUpdateRequestDTO;

import java.util.List;
import java.util.UUID;

public interface DestinationService {

    DestinationResponseDTO create(DestinationCreateRequestDTO dto);

    DestinationResponseDTO getById(UUID id);

    List<DestinationResponseDTO> getAll();

    DestinationResponseDTO update(UUID id, DestinationUpdateRequestDTO dto);

    void delete(UUID id);
}
