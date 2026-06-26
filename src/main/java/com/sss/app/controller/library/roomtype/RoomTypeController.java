package com.sss.app.controller.library.roomtype;

import com.sss.app.dto.library.roomtype.RoomTypeCreateRequestDTO;
import com.sss.app.dto.library.roomtype.RoomTypeResponseDTO;
import com.sss.app.dto.library.roomtype.RoomTypeUpdateRequestDTO;
import com.sss.app.service.library.roomtype.RoomTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/room-types")
@RequiredArgsConstructor
public class RoomTypeController {

    private final RoomTypeService roomTypeService;

    @PostMapping
    public ResponseEntity<RoomTypeResponseDTO> create(@Valid @RequestBody RoomTypeCreateRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roomTypeService.create(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoomTypeResponseDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(roomTypeService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<RoomTypeResponseDTO>> getAll() {
        return ResponseEntity.ok(roomTypeService.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoomTypeResponseDTO> update(@PathVariable UUID id,
                                                        @RequestBody RoomTypeUpdateRequestDTO dto) {
        return ResponseEntity.ok(roomTypeService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        roomTypeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
