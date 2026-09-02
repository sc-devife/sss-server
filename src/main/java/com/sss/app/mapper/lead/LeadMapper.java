package com.sss.app.mapper.lead;

import com.sss.app.dto.lead.LeadCreateRequestDTO;
import com.sss.app.dto.lead.LeadResponseDTO;
import com.sss.app.entity.lead.Lead;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface LeadMapper {
    // Automatically maps fields with same names. escapePoints isn't
    // name-matched to the DTO's escapePointIds (a List<String> of uids) —
    // resolved manually in LeadsHelper/LeadServiceImpl, same pattern as
    // Hotel/Activity's escapePoint. DTO.agencyDetails has no matching entity
    // field at all — Lead doesn't carry it (it's a separate 1:1 record,
    // LeadAgencyDetails, populated/read manually in LeadsHelper/
    // LeadServiceImpl) — MapStruct just leaves it unmapped on toEntity since
    // there's nothing to map it onto, and on toResponse it needs an explicit
    // ignore since the target DTO field does exist there (inherited from
    // LeadDTO) but the source entity has no matching source to auto-map from.
    @Mapping(target = "escapePoints", ignore = true)
    Lead toEntity(LeadCreateRequestDTO dto);

    @Mapping(target = "agencyDetails", ignore = true)
    @Mapping(target = "escapePointIds", ignore = true)
    LeadResponseDTO toResponse(Lead entity);

    // For updates — copy values from DTO into an existing entity
    @Mapping(target = "escapePoints", ignore = true)
    void updateEntityFromDto(LeadCreateRequestDTO dto, @MappingTarget Lead entity);
}
