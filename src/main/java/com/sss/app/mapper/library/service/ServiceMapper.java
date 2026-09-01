package com.sss.app.mapper.library.service;

import com.sss.app.dto.library.service.ServiceCreateRequestDTO;
import com.sss.app.dto.library.service.ServiceResponseDTO;
import com.sss.app.dto.library.service.ServiceUpdateRequestDTO;
import com.sss.app.entity.library.service.Service;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface ServiceMapper {

    // `hotel` (the scope relation) is resolved and set by the service layer,
    // same pattern as Hotel's own relation fields being ignored here and
    // wired in HotelHelper instead.
    @Mapping(target = "hotel", ignore = true)
    Service toEntityCreate(ServiceCreateRequestDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(ServiceUpdateRequestDTO dto, @MappingTarget Service entity);

    @Mapping(target = "hotelId", source = "hotel.uid")
    ServiceResponseDTO toResponse(Service entity);
}
