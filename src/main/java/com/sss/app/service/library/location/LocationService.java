package com.sss.app.service.library.location;

import com.sss.app.dto.library.location.LocationCreateRequestDTO;
import com.sss.app.dto.library.location.LocationResponseDTO;
import com.sss.app.dto.library.location.LocationUpdateRequestDTO;

import java.util.List;
import java.util.UUID;

public interface LocationService {

    LocationResponseDTO create(LocationCreateRequestDTO dto);

    LocationResponseDTO getById(UUID id);

    List<LocationResponseDTO> getAll();

    LocationResponseDTO update(UUID id, LocationUpdateRequestDTO dto);

    void delete(UUID id);
}
