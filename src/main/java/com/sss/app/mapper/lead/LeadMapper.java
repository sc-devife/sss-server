package com.sss.app.mapper.lead;

import com.sss.app.dto.lead.LeadCreateRequestDTO;
import com.sss.app.dto.lead.LeadResponseDTO;
import com.sss.app.entity.lead.Lead;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface LeadMapper {
    // Automatically maps fields with same names. escapePointRef isn't
    // name-matched to the DTO's escapePointId (a String uid) — resolved
    // manually in LeadsHelper, same pattern as Hotel/Activity's escapePoint.
    @Mapping(target = "escapePointRef", ignore = true)
    Lead toEntity(LeadCreateRequestDTO dto);

    LeadResponseDTO toResponse(Lead entity);

    // For updates — copy values from DTO into an existing entity
    @Mapping(target = "escapePointRef", ignore = true)
    void updateEntityFromDto(LeadCreateRequestDTO dto, @MappingTarget Lead entity);
}
