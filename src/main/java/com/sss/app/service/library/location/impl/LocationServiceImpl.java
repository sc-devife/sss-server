package com.sss.app.service.library.location.impl;

import com.sss.app.dto.library.location.LocationCreateRequestDTO;
import com.sss.app.dto.library.location.LocationResponseDTO;
import com.sss.app.dto.library.location.LocationUpdateRequestDTO;
import com.sss.app.entity.library.location.Location;
import com.sss.app.entity.users.User;
import com.sss.app.exception.ResourceNotFoundException;
import com.sss.app.mapper.library.location.LocationMapper;
import com.sss.app.repository.library.location.LocationRepository;
import com.sss.app.security.OrgAccessGuard;
import com.sss.app.service.library.location.LocationService;
import jakarta.persistence.EntityExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class LocationServiceImpl implements LocationService {

    private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;
    private final OrgAccessGuard orgAccessGuard;

    private User currentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Override
    public LocationResponseDTO create(LocationCreateRequestDTO dto) {
        if (locationRepository.existsByDisplayNameIgnoreCase(dto.getDisplayName())) {
            throw new EntityExistsException("Location already exists with displayName: " + dto.getDisplayName());
        }
        Location location = locationMapper.toEntityCreate(dto);
        location.setOrgId(currentUser().getOrgId());
        Location saved = locationRepository.save(location);
        return locationMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public LocationResponseDTO getById(UUID id) {
        Location location = findEntityById(id);
        return locationMapper.toResponse(location);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocationResponseDTO> getAll() {
        return locationRepository.findAllByOrgId(currentUser().getOrgId())
                .stream()
                .map(locationMapper::toResponse)
                .toList();
    }

    @Override
    public LocationResponseDTO update(UUID id, LocationUpdateRequestDTO dto) {
        Location location = findEntityById(id);
        locationMapper.updateEntityFromDto(dto, location);
        Location saved = locationRepository.save(location);
        return locationMapper.toResponse(saved);
    }

    @Override
    public void delete(UUID id) {
        Location location = findEntityById(id);
        locationRepository.delete(location);
    }

    private Location findEntityById(UUID id) {
        Location location = locationRepository.findByUid(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location", id));
        orgAccessGuard.requireAccessToOrg(location.getOrgId());
        return location;
    }
}
