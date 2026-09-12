package com.sss.app.service.library.amenity;

import com.sss.app.dto.library.amenity.AmenityCreateRequestDTO;
import com.sss.app.dto.library.amenity.AmenityResponseDTO;

import java.util.List;

public interface AmenityService {

    AmenityResponseDTO create(AmenityCreateRequestDTO dto);

    List<AmenityResponseDTO> getAll();
}
