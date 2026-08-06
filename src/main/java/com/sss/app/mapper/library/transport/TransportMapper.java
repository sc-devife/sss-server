package com.sss.app.mapper.library.transport;

import com.sss.app.dto.library.transport.TransportCreateRequestDTO;
import com.sss.app.dto.library.transport.TransportResponseDTO;
import com.sss.app.dto.library.transport.TransportUpdateRequestDTO;
import com.sss.app.entity.library.transport.Transport;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface TransportMapper {

    @Mapping(target = "provider", ignore = true)
    @Mapping(target = "escapePoint", ignore = true)
    Transport toEntityCreate(TransportCreateRequestDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "provider", ignore = true)
    @Mapping(target = "escapePoint", ignore = true)
    void updateEntityFromDto(TransportUpdateRequestDTO dto, @MappingTarget Transport entity);

    TransportResponseDTO toResponse(Transport entity);
}
