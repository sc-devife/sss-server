package com.sss.app.mapper.taxprofile;

import com.sss.app.dto.taxprofile.TaxProfileCreateRequestDTO;
import com.sss.app.dto.taxprofile.TaxProfileResponseDTO;
import com.sss.app.dto.taxprofile.TaxProfileUpdateRequestDTO;
import com.sss.app.entity.taxprofile.TaxProfile;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface TaxProfileMapper {

    TaxProfile toEntityCreate(TaxProfileCreateRequestDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(TaxProfileUpdateRequestDTO dto, @MappingTarget TaxProfile entity);

    TaxProfileResponseDTO toResponse(TaxProfile entity);
}
