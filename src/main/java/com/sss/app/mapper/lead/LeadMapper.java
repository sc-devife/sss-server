package com.sss.app.mapper.lead;

import com.sss.app.dto.lead.LeadCreateRequestDTO;
import com.sss.app.dto.lead.LeadDTO;
import com.sss.app.dto.lead.LeadResponseDTO;
import com.sss.app.entity.lead.Lead;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface LeadMapper {
    // Automatically maps fields with same names
    Lead toEntity(LeadCreateRequestDTO dto);

    LeadResponseDTO toResponse(Lead entity);

    // For updates — copy values from DTO into an existing entity
    void updateEntityFromDto(LeadCreateRequestDTO dto, @MappingTarget Lead entity);
}
