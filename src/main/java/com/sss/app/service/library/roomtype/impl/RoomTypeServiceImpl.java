package com.sss.app.service.library.roomtype.impl;

import com.sss.app.dto.library.roomtype.RoomTypeCreateRequestDTO;
import com.sss.app.dto.library.roomtype.RoomTypeResponseDTO;
import com.sss.app.dto.library.roomtype.RoomTypeUpdateRequestDTO;
import com.sss.app.entity.library.roomtype.RoomType;
import com.sss.app.exception.ResourceNotFoundException;
import com.sss.app.mapper.library.roomtype.RoomTypeMapper;
import com.sss.app.repository.library.roomtype.RoomTypeRepository;
import com.sss.app.service.library.roomtype.RoomTypeService;
import jakarta.persistence.EntityExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RoomTypeServiceImpl implements RoomTypeService {

    private final RoomTypeRepository roomTypeRepository;
    private final RoomTypeMapper roomTypeMapper;

    @Override
    public RoomTypeResponseDTO create(RoomTypeCreateRequestDTO dto) {
        if (roomTypeRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new EntityExistsException("RoomType already exists with name: " + dto.getName());
        }
        RoomType roomType = roomTypeMapper.toEntityCreate(dto);
        RoomType saved = roomTypeRepository.save(roomType);
        return roomTypeMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public RoomTypeResponseDTO getById(UUID id) {
        return roomTypeMapper.toResponse(findEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomTypeResponseDTO> getAll() {
        return roomTypeRepository.findAll()
                .stream()
                .map(roomTypeMapper::toResponse)
                .toList();
    }

    @Override
    public RoomTypeResponseDTO update(UUID id, RoomTypeUpdateRequestDTO dto) {
        RoomType roomType = findEntityById(id);
        roomTypeMapper.updateEntityFromDto(dto, roomType);
        RoomType saved = roomTypeRepository.save(roomType);
        return roomTypeMapper.toResponse(saved);
    }

    @Override
    public void delete(UUID id) {
        roomTypeRepository.delete(findEntityById(id));
    }

    private RoomType findEntityById(UUID id) {
        return roomTypeRepository.findByUid(id)
                .orElseThrow(() -> new ResourceNotFoundException("RoomType", id));
    }
}
