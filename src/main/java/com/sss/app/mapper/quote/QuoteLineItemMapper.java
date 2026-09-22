package com.sss.app.mapper.quote;

import com.sss.app.dto.quote.QuoteLineItemResponseDTO;
import com.sss.app.entity.quote.QuoteLineItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface QuoteLineItemMapper {

    @Mapping(target = "itineraryItemUid", source = "itineraryItem.uid")
    @Mapping(target = "cancellation", source = "isCancellation")
    QuoteLineItemResponseDTO toResponse(QuoteLineItem entity);

    List<QuoteLineItemResponseDTO> toResponseList(List<QuoteLineItem> entities);
}
