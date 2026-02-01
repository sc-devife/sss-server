package com.sss.app.controller.escape;

import com.sss.app.dto.escape.EscapeCreateRequestDTO;
import com.sss.app.dto.escape.EscapeResponseDTO;
import com.sss.app.dto.escape.EscapeUpdateRequestDTO;
import com.sss.app.service.escape.EscapeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/escape")
@RequiredArgsConstructor
public class EscapeController {

    private final EscapeService escapeService;

    @PostMapping("/create")
    public ResponseEntity<EscapeResponseDTO> createTrip(
            @Valid @RequestBody EscapeCreateRequestDTO request) {
        return ResponseEntity.ok(escapeService.createEscape(request));
    }
   @PutMapping("/update/{id}")
    public ResponseEntity<EscapeResponseDTO> updateTrip(
           @PathVariable Long id,
           @Valid @RequestBody EscapeUpdateRequestDTO request) {
        return ResponseEntity.ok(escapeService.updateEscape(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EscapeResponseDTO> getTrip(@PathVariable Long id) {
        return ResponseEntity.ok(escapeService.getTripById(id));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteTrip(@PathVariable Long id) {
        escapeService.deleteEscape(id);
        return ResponseEntity.ok("Trip deleted successfully with id: " + id);
    }
}
