package com.sss.app.controller;

import com.sss.app.dto.roles.RoleResponseDto;
import com.sss.app.entity.roles.Role;
import com.sss.app.entity.users.User;
import com.sss.app.repository.RoleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/roles")
public class RolesController {

    private final RoleRepository roleRepository;

    public RolesController(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @PreAuthorize("@permissionService.hasPermission('roles.manage')")
    @GetMapping
    public ResponseEntity<List<RoleResponseDto>> listAssignableRoles() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Role> roles = roleRepository.findByOrgIdIsNullOrOrgId(user.getOrgId());

        List<RoleResponseDto> dtos = roles.stream().map(role -> {
            RoleResponseDto dto = new RoleResponseDto();
            dto.setSeqp(role.getSeqp());
            dto.setUid(role.getUid());
            dto.setName(role.getName());
            dto.setLabel(role.getLabel());
            dto.setIs_active(role.getIs_active());
            dto.setIs_archived(role.getIs_archived());
            return dto;
        }).toList();

        return ResponseEntity.ok(dtos);
    }
}
