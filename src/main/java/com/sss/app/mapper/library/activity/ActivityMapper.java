package com.sss.app.mapper.library.activity;

import com.sss.app.dto.library.activity.ActivityCreateRequestDTO;
import com.sss.app.dto.library.activity.ActivityResponseDTO;
import com.sss.app.dto.library.activity.ActivityUpdateRequestDTO;
import com.sss.app.entity.library.activity.Activity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface ActivityMapper {

    @Mapping(target = "destination", ignore = true)
    Activity toEntityCreate(ActivityCreateRequestDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "destination", ignore = true)
    void updateEntityFromDto(ActivityUpdateRequestDTO dto, @MappingTarget Activity entity);

    ActivityResponseDTO toResponse(Activity entity);
}
