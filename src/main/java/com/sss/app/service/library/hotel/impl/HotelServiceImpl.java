package com.sss.app.service.library.hotel.impl;

import com.sss.app.dto.library.hotel.HotelCreateRequestDTO;
import com.sss.app.dto.library.hotel.HotelResponseDTO;
import com.sss.app.dto.library.hotel.HotelUpdateRequestDTO;
import com.sss.app.entity.library.hotel.Hotel;
import com.sss.app.entity.users.User;
import com.sss.app.exception.ResourceNotFoundException;
import com.sss.app.helper.library.hotel.HotelHelper;
import com.sss.app.mapper.library.hotel.HotelMapper;
import com.sss.app.repository.library.hotel.HotelRepository;
import com.sss.app.security.OrgAccessGuard;
import com.sss.app.service.files.CloudinaryService;
import com.sss.app.service.library.hotel.HotelService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;
    private final HotelMapper hotelMapper;
    private final HotelHelper hotelHelper;
    private final OrgAccessGuard orgAccessGuard;
    private final CloudinaryService cloudinaryService;

    private User currentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Override
    public HotelResponseDTO create(HotelCreateRequestDTO dto) {
        Hotel hotel = hotelMapper.toEntityCreate(dto);
        hotel.setOrgId(currentUser().getOrgId());

        // Resolve & wire relations (location is required, rest are optional)
        hotelHelper.applyRelations(
                hotel,
                dto.getLocationId(),
                dto.getEscapePointId(),
                dto.getEscapePointIds(),
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
        return hotelRepository.findAllByOrgIdAndDeletedAtIsNull(currentUser().getOrgId())
                .stream()
                .map(hotelMapper::toResponse)
                .toList();
    }

    @Override
    public HotelResponseDTO update(UUID id, HotelUpdateRequestDTO dto) {
        Hotel hotel = findEntityById(id);
        List<String> previousImages = hotel.getImages() == null ? null : new ArrayList<>(hotel.getImages());

        // Update scalar fields (name, stars, checkIn/Out, childAge, isActive)
        hotelMapper.updateEntityFromDto(dto, hotel);

        // Update relations only if provided in the request (partial update friendly)
        hotelHelper.applyRelations(
                hotel,
                dto.getLocationId(),
                dto.getEscapePointId(),
                dto.getEscapePointIds(),
                dto.getMealPlanIds(),
                dto.getRoomTypeIds()
        );

        Hotel saved = hotelRepository.save(hotel);
        cloudinaryService.deleteRemoved(previousImages, saved.getImages());
        return hotelMapper.toResponse(saved);
    }

    @Override
    public void delete(UUID id) {
        Hotel hotel = findEntityById(id);
        hotel.setDeletedAt(java.time.LocalDateTime.now());
        hotel.setStatus("archived");
    }

    private Hotel findEntityById(UUID id) {
        Hotel hotel = hotelRepository.findByUid(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", id));
        orgAccessGuard.requireAccessToOrg(hotel.getOrgId());
        return hotel;
    }
}
