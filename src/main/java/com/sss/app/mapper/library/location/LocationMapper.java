package com.sss.app.mapper.library.location;

import com.sss.app.dto.library.location.LocationCreateRequestDTO;
import com.sss.app.dto.library.location.LocationResponseDTO;
import com.sss.app.dto.library.location.LocationUpdateRequestDTO;
import com.sss.app.entity.library.location.Location;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface LocationMapper {

    Location toEntityCreate(LocationCreateRequestDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(LocationUpdateRequestDTO dto, @MappingTarget Location entity);

    LocationResponseDTO toResponse(Location entity);
}
