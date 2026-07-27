package com.sss.app.mapper.library.serviceprovider;

import com.sss.app.dto.library.serviceprovider.ServiceProviderCreateRequestDTO;
import com.sss.app.dto.library.serviceprovider.ServiceProviderResponseDTO;
import com.sss.app.dto.library.serviceprovider.ServiceProviderUpdateRequestDTO;
import com.sss.app.entity.library.serviceprovider.ServiceProvider;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface ServiceProviderMapper {

    ServiceProvider toEntityCreate(ServiceProviderCreateRequestDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(ServiceProviderUpdateRequestDTO dto, @MappingTarget ServiceProvider entity);

    ServiceProviderResponseDTO toResponse(ServiceProvider entity);
}
