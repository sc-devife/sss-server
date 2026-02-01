package com.sss.app.mapper.traveller;

import com.sss.app.dto.traveller.TravellerCreateRequestDTO;
import com.sss.app.dto.traveller.TravellerResponseDTO;
import com.sss.app.dto.traveller.TravellerUpdateRequestDTO;
import com.sss.app.entity.traveller.Traveller;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface TravellerMapper {

   // Automatically maps fields with same names
   Traveller toEntityCreate(TravellerCreateRequestDTO dto);

   @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
   void updateEntityFromDto(
           TravellerUpdateRequestDTO dto,
           @MappingTarget Traveller entity
   );

   TravellerResponseDTO toResponse(Traveller entity);

}
