package com.sss.app.mapper.library.inclusionexclusion;

import com.sss.app.dto.library.inclusionexclusion.InclusionExclusionResponseDto;
import com.sss.app.entity.library.inclusionexclusion.InclusionExclusion;
import org.mapstruct.Mapper;

import java.util.List;

// Entity construction/updates happen by hand in InclusionExclusionsHelper (org
// scoping + destination resolution need more control than a generated
// bean-mapper gives) — this interface only handles the read-side response shape.
@Mapper(componentModel = "spring")
public interface InclusionExclusionMapper {

    InclusionExclusionResponseDto toDto(InclusionExclusion entity);

    List<InclusionExclusionResponseDto> toDtoList(List<InclusionExclusion> entities);
}
