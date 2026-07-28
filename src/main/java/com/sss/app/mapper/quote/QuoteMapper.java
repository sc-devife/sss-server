package com.sss.app.mapper.quote;

import com.sss.app.dto.quote.QuoteCreateRequestDTO;
import com.sss.app.dto.quote.QuoteResponseDTO;
import com.sss.app.dto.quote.QuoteUpdateRequestDTO;
import com.sss.app.entity.quote.Quote;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface QuoteMapper {

    @Mapping(target = "itinerary", ignore = true)
    Quote toEntityCreate(QuoteCreateRequestDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "itinerary", ignore = true)
    void updateEntityFromDto(QuoteUpdateRequestDTO dto, @MappingTarget Quote entity);

    @Mapping(target = "itineraryUid", source = "itinerary.uid")
    QuoteResponseDTO toResponse(Quote entity);
}
