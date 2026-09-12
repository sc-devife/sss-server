package com.sss.app.service.library.amenity.impl;

import com.sss.app.dto.library.amenity.AmenityCreateRequestDTO;
import com.sss.app.dto.library.amenity.AmenityResponseDTO;
import com.sss.app.entity.library.amenity.Amenity;
import com.sss.app.mapper.library.amenity.AmenityMapper;
import com.sss.app.repository.library.amenity.AmenityRepository;
import jakarta.persistence.EntityExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AmenityServiceImpl implements com.sss.app.service.library.amenity.AmenityService {

    private final AmenityRepository amenityRepository;
    private final AmenityMapper amenityMapper;

    @Override
    public AmenityResponseDTO create(AmenityCreateRequestDTO dto) {
        if (amenityRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new EntityExistsException("Amenity already exists with name: " + dto.getName());
        }
        Amenity amenity = amenityMapper.toEntityCreate(dto);
        Amenity saved = amenityRepository.save(amenity);
        return amenityMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AmenityResponseDTO> getAll() {
        return amenityRepository.findAllByIsActiveTrueOrderByNameAsc().stream()
                .map(amenityMapper::toResponse)
                .toList();
    }
}
