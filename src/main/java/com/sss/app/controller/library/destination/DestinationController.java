package com.sss.app.controller.library.destination;

import com.sss.app.dto.library.destination.DestinationCreateRequestDTO;
import com.sss.app.dto.library.destination.DestinationResponseDTO;
import com.sss.app.dto.library.destination.DestinationUpdateRequestDTO;
import com.sss.app.service.library.destination.DestinationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/destinations")
@RequiredArgsConstructor
public class DestinationController {

    private final DestinationService destinationService;

    @PostMapping
    public ResponseEntity<DestinationResponseDTO> create(@Valid @RequestBody DestinationCreateRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(destinationService.create(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DestinationResponseDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(destinationService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<DestinationResponseDTO>> getAll() {
        return ResponseEntity.ok(destinationService.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<DestinationResponseDTO> update(@PathVariable UUID id,
                                                         @RequestBody DestinationUpdateRequestDTO dto) {
        return ResponseEntity.ok(destinationService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        destinationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
