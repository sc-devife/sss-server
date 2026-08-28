package com.sss.app.service.impl;

import com.sss.app.dto.team.TeamRefDto;
import com.sss.app.dto.users.UserAssignmentSettingsUpdateRequestDto;
import com.sss.app.dto.users.UserCreateRequestDto;
import com.sss.app.dto.users.UserResponseDto;
import com.sss.app.dto.users.UserUpdateRequestDto;
import com.sss.app.entity.UserSession;
import com.sss.app.entity.team.UserTeamLink;
import com.sss.app.entity.users.User;
import com.sss.app.helper.UsersHelper;
import com.sss.app.mapper.UserMapper;
import com.sss.app.repository.OrganizationRepository;
import com.sss.app.repository.UserRepository;
import com.sss.app.repository.UserSessionRepository;
import com.sss.app.repository.team.UserTeamLinkRepository;
import com.sss.app.service.UsersService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UsersServiceImpl implements UsersService {

    UsersHelper usersHelper;
    UserMapper userMapper;
    OrganizationRepository organizationRepository;
    UserSessionRepository userSessionRepository;
    UserRepository userRepository;
    UserTeamLinkRepository userTeamLinkRepository;

    public UsersServiceImpl(UsersHelper usersHelper, UserMapper userMapper, OrganizationRepository organizationRepository,
                             UserSessionRepository userSessionRepository, UserRepository userRepository,
                             UserTeamLinkRepository userTeamLinkRepository) {
        this.usersHelper = usersHelper;
        this.userMapper = userMapper;
        this.organizationRepository = organizationRepository;
        this.userSessionRepository = userSessionRepository;
        this.userRepository = userRepository;
        this.userTeamLinkRepository = userTeamLinkRepository;
    }

    @Override
    public UserResponseDto getCurrentUser() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return toProfileResponseDto(user);
    }

    // Deliberately bypasses updateUser's users.write permission gate — this
    // only ever touches the caller's own record (uid comes from the security
    // principal, never the request), so it's not equivalent to the
    // admin-facing "edit any user" capability that permission guards.
    @Override
    public UserResponseDto updateCurrentUser(UserUpdateRequestDto payload) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User updated = usersHelper.updateUser(user.getUid(), payload);
        return toProfileResponseDto(updated);
    }

    // Joins the caller's own organization through the existing User.orgId
    // relationship — org details are never duplicated onto the users table,
    // just looked up and attached to the response for the self-service
    // profile endpoints (getCurrentUser / updateCurrentUser) only.
    private UserResponseDto toProfileResponseDto(User user) {
        UserResponseDto dto = userMapper.toUserResponseDto(user);
        if (user.getOrgId() != null) {
            organizationRepository.findById(user.getOrgId()).ifPresent(organization -> {
                String displayName = organization.getDisplayName();
                dto.setOrganizationName(
                        (displayName != null && !displayName.isBlank()) ? displayName : organization.getRegisteredName()
                );
                dto.setOrganizationLogo(organization.getLogoFile());
                dto.setOrganizationLogoShape(organization.getLogoShape());
            });
        }
        dto.setTeams(userTeamLinkRepository.findAllByUser_Seqp(user.getSeqp()).stream()
                .filter(link -> Boolean.TRUE.equals(link.getIsActive()))
                .map(link -> new TeamRefDto(link.getTeam().getSeqp(), link.getTeam().getUid().toString(), link.getTeam().getName()))
                .toList());
        return dto;
    }

    @Override
    public List<UserResponseDto> fetchAllUsers(Long companyId) {
        List<User> users = usersHelper.fetchAllUsers(companyId);
        List<UserResponseDto> dtos = userMapper.toUserResponseDtoList(users);

        // A user can hold multiple concurrent sessions now (one per device) —
        // "last active" is the most recent lastAccessed across their active
        // sessions, not a single row keyed by email like before.
        List<Long> sessionUserSeqps = users.stream().map(User::getSeqp).toList();
        Map<Long, LocalDateTime> lastActiveByUserSeqp = userSessionRepository.findAllByUser_SeqpInAndRevokedAtIsNull(sessionUserSeqps).stream()
                .filter(s -> s.getLastAccessed() != null)
                .collect(Collectors.groupingBy(s -> s.getUser().getSeqp(),
                        Collectors.mapping(UserSession::getLastAccessed, Collectors.maxBy(LocalDateTime::compareTo))))
                .entrySet().stream()
                .filter(e -> e.getValue().isPresent())
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get()));

        for (int i = 0; i < users.size(); i++) {
            dtos.get(i).setLastActiveAt(lastActiveByUserSeqp.get(users.get(i).getSeqp()));
        }

        List<Long> inviterIds = users.stream().map(User::getInvitedBy).filter(id -> id != null).toList();
        Map<Long, String> inviterNamesBySeqp = userRepository.findAllById(inviterIds).stream()
                .collect(Collectors.toMap(User::getSeqp, User::getName));
        for (int i = 0; i < users.size(); i++) {
            Long inviterId = users.get(i).getInvitedBy();
            if (inviterId != null) {
                dtos.get(i).setInvitedByName(inviterNamesBySeqp.get(inviterId));
            }
        }

        List<Long> userSeqps = users.stream().map(User::getSeqp).toList();
        Map<Long, List<TeamRefDto>> teamsByUserSeqp = userTeamLinkRepository.findAllByUser_SeqpIn(userSeqps).stream()
                .filter(link -> Boolean.TRUE.equals(link.getIsActive()))
                .collect(Collectors.groupingBy(
                        link -> link.getUser().getSeqp(),
                        Collectors.mapping(link -> new TeamRefDto(link.getTeam().getSeqp(), link.getTeam().getUid().toString(), link.getTeam().getName()), Collectors.toList())
                ));
        for (int i = 0; i < users.size(); i++) {
            dtos.get(i).setTeams(teamsByUserSeqp.getOrDefault(users.get(i).getSeqp(), List.of()));
        }

        return dtos;
    }

    @Override
    public UserResponseDto reassignTeams(String uid, List<String> teamUids) {
        return userMapper.toUserResponseDto(usersHelper.reassignTeams(uid, teamUids));
    }

    @Override
    public UserResponseDto getUserByUid(String uid) {
        return userMapper.toUserResponseDto(usersHelper.getUserByUid(uid));
    }

    @Override
    public UserResponseDto createUser(UserCreateRequestDto dto) {
        return userMapper.toUserResponseDto(usersHelper.createUser(dto));
    }

    @Override
    public UserResponseDto updateUser(String uid, UserUpdateRequestDto payload) {
        return userMapper.toUserResponseDto(usersHelper.updateUser(uid, payload));
    }

    @Override
    public UserResponseDto updateAssignmentSettings(String uid, UserAssignmentSettingsUpdateRequestDto payload) {
        return userMapper.toUserResponseDto(usersHelper.updateAssignmentSettings(uid, payload));
    }

    @Override
    public UserResponseDto reassignRoles(String uid, List<String> roles) {
        return userMapper.toUserResponseDto(usersHelper.reassignRoles(uid, roles));
    }

    @Override
    public UserResponseDto setBlocked(String uid, boolean blocked) {
        return userMapper.toUserResponseDto(usersHelper.setBlocked(uid, blocked));
    }
}
