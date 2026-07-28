package com.sss.app.controller.library.inclusionexclusion;

import com.sss.app.dto.library.inclusionexclusion.InclusionExclusionCreateRequestDto;
import com.sss.app.dto.library.inclusionexclusion.InclusionExclusionResponseDto;
import com.sss.app.dto.library.inclusionexclusion.InclusionExclusionUpdateRequestDto;
import com.sss.app.service.library.inclusionexclusion.InclusionExclusionsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inclusion-exclusions")
@RequiredArgsConstructor
public class InclusionExclusionController {

    private final InclusionExclusionsService inclusionExclusionsService;

    @PreAuthorize("@permissionService.hasPermission('library.read')")
    @GetMapping
    public ResponseEntity<List<InclusionExclusionResponseDto>> getAll(@RequestParam(required = false) String type) {
        return ResponseEntity.ok(inclusionExclusionsService.fetchAllForOrg(type));
    }

    @PreAuthorize("@permissionService.hasPermission('library.read')")
    @GetMapping("/selectable")
    public ResponseEntity<List<InclusionExclusionResponseDto>> getSelectableForItinerary(
            @RequestParam UUID itineraryUid, @RequestParam String type) {
        return ResponseEntity.ok(inclusionExclusionsService.getSelectableForItinerary(itineraryUid, type));
    }

    @PreAuthorize("@permissionService.hasPermission('library.read')")
    @GetMapping("/{uid}")
    public ResponseEntity<InclusionExclusionResponseDto> getByUid(@PathVariable String uid) {
        return ResponseEntity.ok(inclusionExclusionsService.getByUid(uid));
    }

    @PreAuthorize("@permissionService.hasPermission('library.write')")
    @PostMapping
    public ResponseEntity<InclusionExclusionResponseDto> create(@Valid @RequestBody InclusionExclusionCreateRequestDto payload) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inclusionExclusionsService.create(payload));
    }

    @PreAuthorize("@permissionService.hasPermission('library.write')")
    @PutMapping("/{uid}")
    public ResponseEntity<InclusionExclusionResponseDto> update(@PathVariable String uid, @Valid @RequestBody InclusionExclusionUpdateRequestDto payload) {
        return ResponseEntity.ok(inclusionExclusionsService.update(uid, payload));
    }

    @PreAuthorize("@permissionService.hasPermission('library.write')")
    @PatchMapping("/{uid}/deactivate")
    public ResponseEntity<InclusionExclusionResponseDto> deactivate(@PathVariable String uid) {
        return ResponseEntity.ok(inclusionExclusionsService.deactivate(uid));
    }
}
