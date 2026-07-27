package com.sss.app.service.library.serviceprovider;

import com.sss.app.dto.library.serviceprovider.ServiceProviderCreateRequestDTO;
import com.sss.app.dto.library.serviceprovider.ServiceProviderResponseDTO;
import com.sss.app.dto.library.serviceprovider.ServiceProviderUpdateRequestDTO;

import java.util.List;
import java.util.UUID;

public interface ServiceProviderService {
    ServiceProviderResponseDTO create(ServiceProviderCreateRequestDTO dto);

    ServiceProviderResponseDTO getById(UUID id);

    List<ServiceProviderResponseDTO> getAll();

    ServiceProviderResponseDTO update(UUID id, ServiceProviderUpdateRequestDTO dto);

    void delete(UUID id);
}
