package com.sss.app.controller.team;

import com.sss.app.dto.team.TeamCreateRequestDTO;
import com.sss.app.dto.team.TeamResponseDTO;
import com.sss.app.dto.team.TeamUpdateRequestDTO;
import com.sss.app.service.team.TeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

// Reuses the existing users.read/users.write permissions rather than minting
// a new "teams.*" pair — Teams are a user-administration concern (grouping
// users), same gate as inviting/editing/blocking users.
@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @PreAuthorize("@permissionService.hasPermission('users.write')")
    @PostMapping
    public ResponseEntity<TeamResponseDTO> create(@Valid @RequestBody TeamCreateRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(teamService.create(dto));
    }

    @PreAuthorize("@permissionService.hasPermission('users.read')")
    @GetMapping("/{uid}")
    public ResponseEntity<TeamResponseDTO> getById(@PathVariable UUID uid) {
        return ResponseEntity.ok(teamService.getById(uid));
    }

    @PreAuthorize("@permissionService.hasPermission('users.read')")
    @GetMapping
    public ResponseEntity<List<TeamResponseDTO>> getAll() {
        return ResponseEntity.ok(teamService.getAll());
    }

    @PreAuthorize("@permissionService.hasPermission('users.write')")
    @PutMapping("/{uid}")
    public ResponseEntity<TeamResponseDTO> update(@PathVariable UUID uid, @RequestBody TeamUpdateRequestDTO dto) {
        return ResponseEntity.ok(teamService.update(uid, dto));
    }

    @PreAuthorize("@permissionService.hasPermission('users.write')")
    @DeleteMapping("/{uid}")
    public ResponseEntity<Void> delete(@PathVariable UUID uid) {
        teamService.delete(uid);
        return ResponseEntity.noContent().build();
    }
}
