package com.sss.app.controller.library.escapepoint;

import com.sss.app.dto.library.escapepoint.EscapePointCreateRequestDto;
import com.sss.app.dto.library.escapepoint.EscapePointResponseDto;
import com.sss.app.dto.library.escapepoint.EscapePointUpdateRequestDto;
import com.sss.app.service.library.escapepoint.EscapePointsService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/library/escapepoints")
public class EscapePointsController {
    private final EscapePointsService escapePointsService;

    public EscapePointsController(EscapePointsService escapePointsService) {
        this.escapePointsService = escapePointsService;
    }

    @PreAuthorize("@permissionService.hasPermission('library.read')")
    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<EscapePointResponseDto>> fetchAllEscapePoints() {
        return ResponseEntity.ok(escapePointsService.fetchAllEscapePoints());
    }

    // Was a method-unrestricted @RequestMapping, which would collide with the
    // DeleteMapping below on the same "/{uid}" path (Spring rejects that as an
    // ambiguous mapping at startup) — narrowed to GET, which is all this ever needed.
    @PreAuthorize("@permissionService.hasPermission('library.read')")
    @GetMapping(value = "/{uid}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EscapePointResponseDto> getEscapePointByUid(@PathVariable String uid) {
        return ResponseEntity.ok(escapePointsService.getEscapePointByUid(uid));
    }

    @PreAuthorize("@permissionService.hasPermission('library.write')")
    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EscapePointResponseDto> createEscapePoint(@Valid @RequestBody EscapePointCreateRequestDto payload) {
        return ResponseEntity.ok(escapePointsService.createEscapePoint(payload));
    }

    @PreAuthorize("@permissionService.hasPermission('library.write')")
    @PutMapping(value = "{uid}/update", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EscapePointResponseDto> updateEscapePoint(@PathVariable String uid, @Valid @RequestBody EscapePointUpdateRequestDto payload) {
        return ResponseEntity.ok(escapePointsService.updateEscapePoint(uid, payload));
    }

    @PreAuthorize("@permissionService.hasPermission('library.write')")
    @DeleteMapping("/{uid}")
    public ResponseEntity<Void> deleteEscapePoint(@PathVariable String uid) {
        escapePointsService.deleteEscapePoint(uid);
        return ResponseEntity.noContent().build();
    }
}
