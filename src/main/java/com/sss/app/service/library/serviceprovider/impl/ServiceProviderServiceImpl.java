package com.sss.app.service.library.serviceprovider.impl;

import com.sss.app.dto.library.serviceprovider.ServiceProviderCreateRequestDTO;
import com.sss.app.dto.library.serviceprovider.ServiceProviderResponseDTO;
import com.sss.app.dto.library.serviceprovider.ServiceProviderUpdateRequestDTO;
import com.sss.app.entity.library.serviceprovider.ServiceProvider;
import com.sss.app.entity.users.User;
import com.sss.app.exception.ResourceNotFoundException;
import com.sss.app.mapper.library.serviceprovider.ServiceProviderMapper;
import com.sss.app.repository.library.serviceprovider.ServiceProviderRepository;
import com.sss.app.security.OrgAccessGuard;
import com.sss.app.service.library.serviceprovider.ServiceProviderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ServiceProviderServiceImpl implements ServiceProviderService {

    private final ServiceProviderRepository serviceProviderRepository;
    private final ServiceProviderMapper serviceProviderMapper;
    private final OrgAccessGuard orgAccessGuard;

    private User currentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Override
    public ServiceProviderResponseDTO create(ServiceProviderCreateRequestDTO dto) {
        ServiceProvider entity = serviceProviderMapper.toEntityCreate(dto);
        entity.setOrgId(currentUser().getOrgId());
        ServiceProvider saved = serviceProviderRepository.save(entity);
        return serviceProviderMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceProviderResponseDTO getById(UUID id) {
        return serviceProviderMapper.toResponse(findEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceProviderResponseDTO> getAll() {
        return serviceProviderRepository.findAllByOrgIdAndDeletedAtIsNull(currentUser().getOrgId())
                .stream()
                .map(serviceProviderMapper::toResponse)
                .toList();
    }

    @Override
    public ServiceProviderResponseDTO update(UUID id, ServiceProviderUpdateRequestDTO dto) {
        ServiceProvider entity = findEntityById(id);
        serviceProviderMapper.updateEntityFromDto(dto, entity);
        ServiceProvider saved = serviceProviderRepository.save(entity);
        return serviceProviderMapper.toResponse(saved);
    }

    @Override
    public void delete(UUID id) {
        ServiceProvider entity = findEntityById(id);
        entity.setDeletedAt(LocalDateTime.now());
        entity.setStatus("archived");
    }

    private ServiceProvider findEntityById(UUID id) {
        ServiceProvider entity = serviceProviderRepository.findByUid(id)
                .orElseThrow(() -> new ResourceNotFoundException("ServiceProvider", id));
        orgAccessGuard.requireAccessToOrg(entity.getOrgId());
        return entity;
    }
}
