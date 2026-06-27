package com.sss.app.mapper.library.destination;

import com.sss.app.dto.library.destination.DestinationCreateRequestDTO;
import com.sss.app.dto.library.destination.DestinationResponseDTO;
import com.sss.app.dto.library.destination.DestinationUpdateRequestDTO;
import com.sss.app.entity.library.destination.Destination;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface DestinationMapper {

    Destination toEntityCreate(DestinationCreateRequestDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(DestinationUpdateRequestDTO dto, @MappingTarget Destination entity);

    DestinationResponseDTO toResponse(Destination entity);
}
