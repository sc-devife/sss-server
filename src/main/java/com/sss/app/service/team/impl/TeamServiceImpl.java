package com.sss.app.service.team.impl;

import com.sss.app.dto.team.TeamCreateRequestDTO;
import com.sss.app.dto.team.TeamMemberSummaryDto;
import com.sss.app.dto.team.TeamResponseDTO;
import com.sss.app.dto.team.TeamUpdateRequestDTO;
import com.sss.app.entity.team.Team;
import com.sss.app.entity.team.TeamStatus;
import com.sss.app.entity.team.UserTeamLink;
import com.sss.app.entity.users.User;
import com.sss.app.exception.ResourceNotFoundException;
import com.sss.app.mapper.team.TeamMapper;
import com.sss.app.repository.UserRepository;
import com.sss.app.repository.team.TeamRepository;
import com.sss.app.repository.team.UserTeamLinkRepository;
import com.sss.app.security.OrgAccessGuard;
import com.sss.app.service.team.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final UserTeamLinkRepository userTeamLinkRepository;
    private final UserRepository userRepository;
    private final TeamMapper teamMapper;
    private final OrgAccessGuard orgAccessGuard;

    private User currentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Override
    public TeamResponseDTO create(TeamCreateRequestDTO dto) {
        Team entity = teamMapper.toEntityCreate(dto);
        entity.setOrgId(currentUser().getOrgId());
        if (entity.getStatus() == null) {
            entity.setStatus(TeamStatus.ACTIVE);
        }
        Team saved = teamRepository.save(entity);
        return enrichAll(List.of(saved), List.of(teamMapper.toResponse(saved))).get(0);
    }

    @Override
    @Transactional(readOnly = true)
    public TeamResponseDTO getById(UUID uid) {
        Team entity = findEntityByUid(uid);
        return enrichAll(List.of(entity), List.of(teamMapper.toResponse(entity))).get(0);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamResponseDTO> getAll() {
        List<Team> teams = teamRepository.findAllByOrgId(currentUser().getOrgId());
        List<TeamResponseDTO> dtos = teams.stream().map(teamMapper::toResponse).toList();
        return enrichAll(teams, dtos);
    }

    @Override
    public TeamResponseDTO update(UUID uid, TeamUpdateRequestDTO dto) {
        Team entity = findEntityByUid(uid);
        teamMapper.updateEntityFromDto(dto, entity);
        Team saved = teamRepository.save(entity);
        return enrichAll(List.of(saved), List.of(teamMapper.toResponse(saved))).get(0);
    }

    @Override
    public void delete(UUID uid) {
        Team entity = findEntityByUid(uid);
        entity.setStatus(TeamStatus.INACTIVE);
        teamRepository.save(entity);
    }

    private Team findEntityByUid(UUID uid) {
        Team entity = teamRepository.findByUid(uid)
                .orElseThrow(() -> new ResourceNotFoundException("Team", uid));
        orgAccessGuard.requireAccessToOrg(entity.getOrgId());
        return entity;
    }

    // Batch-resolves teamLeadUserName + members across every team in one pass
    // rather than one query per team — same pattern as
    // UsersServiceImpl.fetchAllUsers()'s invitedByName resolution.
    private List<TeamResponseDTO> enrichAll(List<Team> teams, List<TeamResponseDTO> dtos) {
        List<Long> leadIds = teams.stream().map(Team::getTeamLeadUserId).filter(id -> id != null).toList();
        Map<Long, String> leadNamesBySeqp = userRepository.findAllById(leadIds).stream()
                .collect(Collectors.toMap(User::getSeqp, User::getName));

        List<Long> teamSeqps = teams.stream().map(Team::getSeqp).toList();
        Map<Long, List<TeamMemberSummaryDto>> membersByTeamSeqp = userTeamLinkRepository.findAllByTeam_SeqpIn(teamSeqps).stream()
                .filter(link -> Boolean.TRUE.equals(link.getIsActive()))
                .collect(Collectors.groupingBy(
                        link -> link.getTeam().getSeqp(),
                        Collectors.mapping(link -> {
                            User u = link.getUser();
                            return new TeamMemberSummaryDto(u.getSeqp(), u.getUid(), u.getName(), u.getEmail());
                        }, Collectors.toList())
                ));

        for (int i = 0; i < teams.size(); i++) {
            Team team = teams.get(i);
            TeamResponseDTO dto = dtos.get(i);
            if (team.getTeamLeadUserId() != null) {
                dto.setTeamLeadUserName(leadNamesBySeqp.get(team.getTeamLeadUserId()));
            }
            dto.setMembers(membersByTeamSeqp.getOrDefault(team.getSeqp(), List.of()));
        }
        return dtos;
    }
}
