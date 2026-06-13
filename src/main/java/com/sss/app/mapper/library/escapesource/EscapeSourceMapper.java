package com.sss.app.mapper.library.escapesource;

import com.sss.app.dto.library.escapesource.EscapeSourceRequestDTO;
import com.sss.app.dto.library.escapesource.EscapeSourceResponseDTO;
import com.sss.app.entity.library.escapesource.EscapeSource;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EscapeSourceMapper {

    EscapeSourceResponseDTO toDto(EscapeSource escapePoint);
    EscapeSource toEntity(EscapeSourceRequestDTO dto);

}
