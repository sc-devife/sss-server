package com.sss.app.service.library.roomtype;

import com.sss.app.dto.library.roomtype.RoomTypeCreateRequestDTO;
import com.sss.app.dto.library.roomtype.RoomTypeResponseDTO;
import com.sss.app.dto.library.roomtype.RoomTypeUpdateRequestDTO;

import java.util.List;
import java.util.UUID;

public interface RoomTypeService {

    RoomTypeResponseDTO create(RoomTypeCreateRequestDTO dto);

    RoomTypeResponseDTO getById(UUID id);

    List<RoomTypeResponseDTO> getAll();

    RoomTypeResponseDTO update(UUID id, RoomTypeUpdateRequestDTO dto);

    void delete(UUID id);
}
