package com.sss.app.mapper.library.escapepoint;

import com.sss.app.dto.library.escapepoint.EscapePointCreateRequestDto;
import com.sss.app.dto.library.escapepoint.EscapePointResponseDto;
import com.sss.app.dto.library.escapepoint.EscapePointUpdateRequestDto;
import com.sss.app.entity.library.escapepoint.EscapePoint;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EscapePointMapper {
    EscapePointMapper INSTANCE = Mappers.getMapper(EscapePointMapper.class);

    // IGNORE (matching HotelMapper/ActivityMapper): an update request that
    // omits images should leave the existing gallery untouched, not null it
    // out — that distinction now also determines whether Cloudinary cleanup
    // (see EscapePointsHelper.updateEscapePoint) treats every existing image
    // as "removed".
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(EscapePointUpdateRequestDto dto, @MappingTarget EscapePoint escapePoint);

    // locations/locationLabel resolved separately (batched across all escape
    // points in a response) — see EscapePointsServiceImpl.
    @Mapping(target = "locations", ignore = true)
    @Mapping(target = "locationLabel", ignore = true)
    EscapePointResponseDto toDto(EscapePoint escapePoint);

    List<EscapePointResponseDto> toDtoList(List<EscapePoint> entities);

    EscapePoint toEntity(EscapePointCreateRequestDto dto);

}
