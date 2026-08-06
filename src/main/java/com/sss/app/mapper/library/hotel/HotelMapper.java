package com.sss.app.mapper.library.hotel;

import com.sss.app.dto.library.hotel.HotelCreateRequestDTO;
import com.sss.app.dto.library.hotel.HotelResponseDTO;
import com.sss.app.dto.library.hotel.HotelUpdateRequestDTO;
import com.sss.app.entity.library.hotel.Hotel;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * Note: locationId / escapePointIds / mealPlanIds / roomTypeIds are NOT mapped here.
 * MapStruct can't resolve IDs to entities without a DB lookup, so those associations
 * are wired manually in HotelHelper before/after this mapper runs.
 */
@Mapper(componentModel = "spring")
public interface HotelMapper {

    @Mapping(target = "location", ignore = true)
    @Mapping(target = "escapePoint", ignore = true)
    @Mapping(target = "escapePoints", ignore = true)
    @Mapping(target = "mealPlans", ignore = true)
    @Mapping(target = "roomTypes", ignore = true)
    Hotel toEntityCreate(HotelCreateRequestDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "location", ignore = true)
    @Mapping(target = "escapePoint", ignore = true)
    @Mapping(target = "escapePoints", ignore = true)
    @Mapping(target = "mealPlans", ignore = true)
    @Mapping(target = "roomTypes", ignore = true)
    void updateEntityFromDto(HotelUpdateRequestDTO dto, @MappingTarget Hotel entity);

    HotelResponseDTO toResponse(Hotel entity);
}
