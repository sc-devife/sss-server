package com.sss.app.service.library.service.impl;

import com.sss.app.dto.library.service.ServiceCreateRequestDTO;
import com.sss.app.dto.library.service.ServiceResponseDTO;
import com.sss.app.dto.library.service.ServiceUpdateRequestDTO;
import com.sss.app.entity.library.hotel.Hotel;
import com.sss.app.entity.library.service.Service;
import com.sss.app.exception.ResourceNotFoundException;
import com.sss.app.mapper.library.service.ServiceMapper;
import com.sss.app.repository.library.hotel.HotelRepository;
import com.sss.app.repository.library.service.ServiceRepository;
import jakarta.persistence.EntityExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
@Transactional
public class ServiceServiceImpl implements com.sss.app.service.library.service.ServiceService {

    private final ServiceRepository serviceRepository;
    private final ServiceMapper serviceMapper;
    private final HotelRepository hotelRepository;

    @Override
    public ServiceResponseDTO create(ServiceCreateRequestDTO dto) {
        Hotel scopeHotel = null;
        if (dto.getHotelId() != null) {
            scopeHotel = hotelRepository.findByUid(dto.getHotelId())
                    .orElseThrow(() -> new ResourceNotFoundException("Hotel", dto.getHotelId()));
            if (serviceRepository.existsByNameIgnoreCaseVisibleToHotel(dto.getName(), dto.getHotelId())) {
                throw new EntityExistsException("Service already exists with name: " + dto.getName());
            }
        } else if (serviceRepository.existsByNameIgnoreCaseAndHotelIsNull(dto.getName())) {
            throw new EntityExistsException("Service already exists with name: " + dto.getName());
        }
        Service service = serviceMapper.toEntityCreate(dto);
        service.setHotel(scopeHotel);
        Service saved = serviceRepository.save(service);
        return serviceMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceResponseDTO getById(UUID id) {
        return serviceMapper.toResponse(findEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceResponseDTO> getAll(UUID hotelId) {
        List<Service> results = hotelId != null
                ? serviceRepository.findAllVisibleToHotel(hotelId)
                : serviceRepository.findAllByHotelIsNull();
        return results.stream()
                .map(serviceMapper::toResponse)
                .toList();
    }

    @Override
    public ServiceResponseDTO update(UUID id, ServiceUpdateRequestDTO dto) {
        Service service = findEntityById(id);
        serviceMapper.updateEntityFromDto(dto, service);
        Service saved = serviceRepository.save(service);
        return serviceMapper.toResponse(saved);
    }

    @Override
    public void delete(UUID id) {
        serviceRepository.delete(findEntityById(id));
    }

    private Service findEntityById(UUID id) {
        return serviceRepository.findByUid(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service", id));
    }
}
