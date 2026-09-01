package com.sss.app.service.library.service;

import com.sss.app.dto.library.service.ServiceCreateRequestDTO;
import com.sss.app.dto.library.service.ServiceResponseDTO;
import com.sss.app.dto.library.service.ServiceUpdateRequestDTO;

import java.util.List;
import java.util.UUID;

public interface ServiceService {

    ServiceResponseDTO create(ServiceCreateRequestDTO dto);

    ServiceResponseDTO getById(UUID id);

    // hotelId null -> global master-data services only (the main Services
    // module's list). hotelId set -> global services plus that hotel's own
    // scoped ones (what its Add/Edit form's Services picker should offer).
    List<ServiceResponseDTO> getAll(UUID hotelId);

    ServiceResponseDTO update(UUID id, ServiceUpdateRequestDTO dto);

    void delete(UUID id);
}
