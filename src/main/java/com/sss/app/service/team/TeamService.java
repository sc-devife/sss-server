package com.sss.app.service.team;

import com.sss.app.dto.team.TeamCreateRequestDTO;
import com.sss.app.dto.team.TeamResponseDTO;
import com.sss.app.dto.team.TeamUpdateRequestDTO;

import java.util.List;
import java.util.UUID;

public interface TeamService {

    TeamResponseDTO create(TeamCreateRequestDTO dto);

    TeamResponseDTO getById(UUID uid);

    List<TeamResponseDTO> getAll();

    TeamResponseDTO update(UUID uid, TeamUpdateRequestDTO dto);

    void delete(UUID uid);
}
