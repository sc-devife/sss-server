package com.sss.app.service.library.hotel;

import com.sss.app.dto.library.hotel.HotelCreateRequestDTO;
import com.sss.app.dto.library.hotel.HotelResponseDTO;
import com.sss.app.dto.library.hotel.HotelUpdateRequestDTO;

import java.util.List;
import java.util.UUID;

public interface HotelService {

    HotelResponseDTO create(HotelCreateRequestDTO dto);

    HotelResponseDTO getById(UUID id);

    List<HotelResponseDTO> getAll();

    HotelResponseDTO update(UUID id, HotelUpdateRequestDTO dto);

    void delete(UUID id);
}
