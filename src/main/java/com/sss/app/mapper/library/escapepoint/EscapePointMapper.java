package com.sss.app.mapper.library.escapepoint;

import com.sss.app.dto.library.escapepoint.EscapePointCreateRequestDto;
import com.sss.app.dto.library.escapepoint.EscapePointResponseDto;
import com.sss.app.dto.library.escapepoint.EscapePointUpdateRequestDto;
import com.sss.app.entity.library.escapepoint.EscapePoint;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EscapePointMapper {
    EscapePointMapper INSTANCE = Mappers.getMapper(EscapePointMapper.class);

    void updateFromDto(EscapePointUpdateRequestDto dto, @MappingTarget EscapePoint escapePoint);

    EscapePointResponseDto toDto(EscapePoint escapePoint);

    List<EscapePointResponseDto> toDtoList(List<EscapePoint> entities);

    EscapePoint toEntity(EscapePointCreateRequestDto dto);

}
