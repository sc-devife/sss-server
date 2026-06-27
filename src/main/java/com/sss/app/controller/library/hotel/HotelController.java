package com.sss.app.controller.library.hotel;

import com.sss.app.dto.library.hotel.HotelCreateRequestDTO;
import com.sss.app.dto.library.hotel.HotelResponseDTO;
import com.sss.app.dto.library.hotel.HotelUpdateRequestDTO;
import com.sss.app.service.library.hotel.HotelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hotels")
@RequiredArgsConstructor
public class HotelController {

    private final HotelService hotelService;

    @PostMapping
    public ResponseEntity<HotelResponseDTO> create(@Valid @RequestBody HotelCreateRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(hotelService.create(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<HotelResponseDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(hotelService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<HotelResponseDTO>> getAll() {
        return ResponseEntity.ok(hotelService.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<HotelResponseDTO> update(@PathVariable UUID id,
                                                     @RequestBody HotelUpdateRequestDTO dto) {
        return ResponseEntity.ok(hotelService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        hotelService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
