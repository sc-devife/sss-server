package com.sss.app.mapper.library.serviceprovider;

import com.sss.app.dto.library.serviceprovider.ServiceProviderCreateRequestDTO;
import com.sss.app.dto.library.serviceprovider.ServiceProviderResponseDTO;
import com.sss.app.dto.library.serviceprovider.ServiceProviderUpdateRequestDTO;
import com.sss.app.entity.library.serviceprovider.ServiceProvider;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface ServiceProviderMapper {

    @Mapping(target = "escapePoint", ignore = true)
    ServiceProvider toEntityCreate(ServiceProviderCreateRequestDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "escapePoint", ignore = true)
    void updateEntityFromDto(ServiceProviderUpdateRequestDTO dto, @MappingTarget ServiceProvider entity);

    ServiceProviderResponseDTO toResponse(ServiceProvider entity);
}
