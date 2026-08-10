package com.sss.app.controller.library.escapesource;

import com.sss.app.dto.library.escapesource.EscapeSourceRequestDTO;
import com.sss.app.dto.library.escapesource.EscapeSourceResponseDTO;
import com.sss.app.service.library.escapesource.EscapeSourceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

//import java.awt.print.Pageable;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/library/escapesource")
public class EscapeSourceController {
    private final EscapeSourceService escapeSourceService;

    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EscapeSourceResponseDTO> createEscapeSource(@Valid @RequestBody EscapeSourceRequestDTO payload) {
        return ResponseEntity.ok(escapeSourceService.createEscapeSource(payload));
    }
    @PutMapping(value = "/{id}/update", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EscapeSourceResponseDTO> updateEscapeSource(
            @PathVariable UUID id,
            @Valid @RequestBody EscapeSourceRequestDTO payload) {

        return ResponseEntity.ok(escapeSourceService.updateEscapeSource(id, payload));
    }
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EscapeSourceResponseDTO> getSourceById(@PathVariable UUID id) {
        return ResponseEntity.ok(escapeSourceService.getSourceById(id));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<EscapeSourceResponseDTO>> getAllSources() {
        return ResponseEntity.ok(escapeSourceService.getAllSources());
    }
    @GetMapping("/paged")
    public ResponseEntity<Page<EscapeSourceResponseDTO>> getPagedSources(Pageable pageable) {
        return ResponseEntity.ok(escapeSourceService.getSources(pageable));
    }
}
