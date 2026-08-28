package com.sss.app.mapper.team;

import com.sss.app.dto.team.TeamCreateRequestDTO;
import com.sss.app.dto.team.TeamResponseDTO;
import com.sss.app.dto.team.TeamUpdateRequestDTO;
import com.sss.app.entity.team.Team;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface TeamMapper {

    Team toEntityCreate(TeamCreateRequestDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(TeamUpdateRequestDTO dto, @MappingTarget Team entity);

    @Mapping(target = "teamLeadUserName", ignore = true)
    @Mapping(target = "members", ignore = true)
    TeamResponseDTO toResponse(Team entity);
}
