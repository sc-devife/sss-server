package com.sss.app.service.library.hotel.impl;

import com.sss.app.dto.library.hotel.HotelCreateRequestDTO;
import com.sss.app.dto.library.hotel.HotelResponseDTO;
import com.sss.app.dto.library.hotel.HotelUpdateRequestDTO;
import com.sss.app.entity.library.hotel.Hotel;
import com.sss.app.exception.ResourceNotFoundException;
import com.sss.app.helper.library.hotel.HotelHelper;
import com.sss.app.mapper.library.hotel.HotelMapper;
import com.sss.app.repository.library.hotel.HotelRepository;
import com.sss.app.service.library.hotel.HotelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;
    private final HotelMapper hotelMapper;
    private final HotelHelper hotelHelper;

    @Override
    public HotelResponseDTO create(HotelCreateRequestDTO dto) {
        Hotel hotel = hotelMapper.toEntityCreate(dto);

        // Resolve & wire relations (location is required, rest are optional)
        hotelHelper.applyRelations(
                hotel,
                dto.getLocationId(),
                dto.getDestinationIds(),
                dto.getMealPlanIds(),
                dto.getRoomTypeIds()
        );

        Hotel saved = hotelRepository.save(hotel);
        return hotelMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public HotelResponseDTO getById(UUID id) {
        return hotelMapper.toResponse(findEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<HotelResponseDTO> getAll() {
        return hotelRepository.findAll()
                .stream()
                .map(hotelMapper::toResponse)
                .toList();
    }

    @Override
    public HotelResponseDTO update(UUID id, HotelUpdateRequestDTO dto) {
        Hotel hotel = findEntityById(id);

        // Update scalar fields (name, stars, checkIn/Out, childAge, isActive)
        hotelMapper.updateEntityFromDto(dto, hotel);

        // Update relations only if provided in the request (partial update friendly)
        hotelHelper.applyRelations(
                hotel,
                dto.getLocationId(),
                dto.getDestinationIds(),
                dto.getMealPlanIds(),
                dto.getRoomTypeIds()
        );

        Hotel saved = hotelRepository.save(hotel);
        return hotelMapper.toResponse(saved);
    }

    @Override
    public void delete(UUID id) {
        Hotel hotel = findEntityById(id);
        hotelRepository.delete(hotel);
    }

    private Hotel findEntityById(UUID id) {
        return hotelRepository.findByUid(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", id));
    }
}
