package com.sss.app.controller.library.inclusionexclusion;

import com.sss.app.dto.library.inclusionexclusion.InclusionExclusionCreateRequestDto;
import com.sss.app.dto.library.inclusionexclusion.InclusionExclusionResponseDto;
import com.sss.app.dto.library.inclusionexclusion.InclusionExclusionUpdateRequestDto;
import com.sss.app.service.library.inclusionexclusion.InclusionExclusionsService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/library/inclusionexclusions")
public class InclusionExclusionController {
    private final InclusionExclusionsService inclusionExclusionsService;

    public InclusionExclusionController(InclusionExclusionsService inclusionExclusionsService) {
        this.inclusionExclusionsService = inclusionExclusionsService;
    }

    @RequestMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<InclusionExclusionResponseDto>> fetchAllInclusionExclusions() {
        return ResponseEntity.ok(inclusionExclusionsService.fetchAllInclusionExclusions(123456L));
    }

    @RequestMapping(value = "/inclusions/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<InclusionExclusionResponseDto>> fetchAllInclusions() {
        return ResponseEntity.ok(inclusionExclusionsService.fetchAllInclusions(123456L));
    }

    @RequestMapping(value = "/exclusions/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<InclusionExclusionResponseDto>> fetchAllExclusions() {
        return ResponseEntity.ok(inclusionExclusionsService.fetchAllExclusions(123456L));
    }

    @RequestMapping(value = "/{uid}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<InclusionExclusionResponseDto> getInclusionExclusionByUid(@PathVariable String uid) {
        return ResponseEntity.ok(inclusionExclusionsService.getInclusionExclusionByUid(uid));
    }

    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<InclusionExclusionResponseDto> createInclusionExclusion(@Valid @RequestBody InclusionExclusionCreateRequestDto payload) {
        return ResponseEntity.ok(inclusionExclusionsService.createInclusionExclusion(payload));
    }

    @PutMapping(value = "{uid}/update", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<InclusionExclusionResponseDto> updateInclusionExclusion(@PathVariable String uid, @Valid @RequestBody InclusionExclusionUpdateRequestDto payload) {
        System.out.println("Incoming payload: " + payload);
        return ResponseEntity.ok(inclusionExclusionsService.updateInclusionExclusion(uid, payload));
    }
}
