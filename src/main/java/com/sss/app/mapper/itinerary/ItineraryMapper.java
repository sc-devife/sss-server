package com.sss.app.mapper.itinerary;

import com.sss.app.dto.itinerary.ItineraryResponseDTO;
import com.sss.app.entity.itinerary.Itinerary;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ItineraryMapper {

    @Mapping(target = "escapeId", source = "escape.seqp")
    ItineraryResponseDTO toResponse(Itinerary entity);
}
