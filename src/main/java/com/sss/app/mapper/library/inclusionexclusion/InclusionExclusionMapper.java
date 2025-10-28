package com.sss.app.mapper.library.inclusionexclusion;

import com.sss.app.dto.library.escapepoint.EscapePointResponseDto;
import com.sss.app.dto.library.inclusionexclusion.InclusionExclusionCreateRequestDto;
import com.sss.app.dto.library.inclusionexclusion.InclusionExclusionResponseDto;
import com.sss.app.dto.library.inclusionexclusion.InclusionExclusionUpdateRequestDto;
import com.sss.app.entity.library.inclusionexclusion.InclusionExclusion;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface InclusionExclusionMapper {
    InclusionExclusionMapper INSTANCE = Mappers.getMapper(InclusionExclusionMapper.class);

    void updateFromDto(InclusionExclusionUpdateRequestDto dto, @MappingTarget InclusionExclusion inclusionExclusion);

    InclusionExclusionResponseDto toDto(InclusionExclusion inclusionExclusion);

    List<InclusionExclusionResponseDto> toDtoList(List<InclusionExclusion> entities);

    InclusionExclusion toEntity(InclusionExclusionCreateRequestDto dto);

}
