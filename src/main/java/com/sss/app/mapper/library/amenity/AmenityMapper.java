package com.sss.app.mapper.library.amenity;

import com.sss.app.dto.library.amenity.AmenityCreateRequestDTO;
import com.sss.app.dto.library.amenity.AmenityResponseDTO;
import com.sss.app.entity.library.amenity.Amenity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AmenityMapper {

    Amenity toEntityCreate(AmenityCreateRequestDTO dto);

    AmenityResponseDTO toResponse(Amenity entity);
}
