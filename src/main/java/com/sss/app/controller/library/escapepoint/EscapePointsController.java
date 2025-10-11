package com.sss.app.controller.library.escapepoint;

import com.sss.app.dto.library.escapepoint.EscapePointCreateRequestDto;
import com.sss.app.dto.library.escapepoint.EscapePointResponseDto;
import com.sss.app.dto.library.escapepoint.EscapePointUpdateRequestDto;
import com.sss.app.service.library.escapepoint.EscapePointsService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/library/escapepoints")
public class EscapePointsController {
    private final EscapePointsService escapePointsService;

    public EscapePointsController(EscapePointsService escapePointsService) {
        this.escapePointsService = escapePointsService;
    }

    @RequestMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<EscapePointResponseDto>> fetchAllEscapePoints() {
        return ResponseEntity.ok(escapePointsService.fetchAllEscapePoints(123456L));
    }

    @RequestMapping(value = "/{uid}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EscapePointResponseDto> getEscapePointByUid(@PathVariable String uid) {
        return ResponseEntity.ok(escapePointsService.getEscapePointByUid(uid));
    }

    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EscapePointResponseDto> createEscapePoint(@Valid @RequestBody EscapePointCreateRequestDto payload) {
        return ResponseEntity.ok(escapePointsService.createEscapePoint(payload));
    }

    @PutMapping(value = "{uid}/update", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EscapePointResponseDto> updateEscapePoint(@PathVariable String uid, @Valid @RequestBody EscapePointUpdateRequestDto payload) {
        System.out.println("Incoming payload: " + payload);
        return ResponseEntity.ok(escapePointsService.updateEscapePoint(uid, payload));
    }
}
