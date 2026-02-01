package com.sss.app.mapper.escape;

import com.sss.app.dto.escape.EscapeCreateRequestDTO;
import com.sss.app.dto.escape.EscapeResponseDTO;
import com.sss.app.dto.escape.EscapeUpdateRequestDTO;
import com.sss.app.entity.escape.Escape;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface EscapeMapper {
    Escape toEntityCreate(EscapeCreateRequestDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(EscapeUpdateRequestDTO dto, @MappingTarget Escape entity);

    EscapeResponseDTO toResponse(Escape entity);
}
