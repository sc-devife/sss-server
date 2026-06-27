package com.sss.app.service.library.destination.impl;

import com.sss.app.dto.library.destination.DestinationCreateRequestDTO;
import com.sss.app.dto.library.destination.DestinationResponseDTO;
import com.sss.app.dto.library.destination.DestinationUpdateRequestDTO;
import com.sss.app.entity.library.destination.Destination;
import com.sss.app.exception.ResourceNotFoundException;
import com.sss.app.mapper.library.destination.DestinationMapper;
import com.sss.app.repository.library.destination.DestinationRepository;
import com.sss.app.service.library.destination.DestinationService;
import jakarta.persistence.EntityExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class DestinationServiceImpl implements DestinationService {

    private final DestinationRepository destinationRepository;
    private final DestinationMapper destinationMapper;

    @Override
    public DestinationResponseDTO create(DestinationCreateRequestDTO dto) {
        if (destinationRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new EntityExistsException("Destination already exists with name: " + dto.getName());
        }
        Destination destination = destinationMapper.toEntityCreate(dto);
        Destination saved = destinationRepository.save(destination);
        return destinationMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public DestinationResponseDTO getById(UUID id) {
        return destinationMapper.toResponse(findEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DestinationResponseDTO> getAll() {
        return destinationRepository.findAll()
                .stream()
                .map(destinationMapper::toResponse)
                .toList();
    }

    @Override
    public DestinationResponseDTO update(UUID id, DestinationUpdateRequestDTO dto) {
        Destination destination = findEntityById(id);
        destinationMapper.updateEntityFromDto(dto, destination);
        Destination saved = destinationRepository.save(destination);
        return destinationMapper.toResponse(saved);
    }

    @Override
    public void delete(UUID id) {
        destinationRepository.delete(findEntityById(id));
    }

    private Destination findEntityById(UUID id) {
        return destinationRepository.findByUid(id)
                .orElseThrow(() -> new ResourceNotFoundException("Destination", id));
    }
}
