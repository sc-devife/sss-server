package com.sss.app.mapper.deal;

import com.sss.app.dto.deal.DealResponseDTO;
import com.sss.app.entity.deal.Deal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DealMapper {

    @Mapping(target = "escapeId", source = "escape.seqp")
    @Mapping(target = "acceptedQuoteUid", source = "acceptedQuote.uid")
    DealResponseDTO toResponse(Deal entity);
}
