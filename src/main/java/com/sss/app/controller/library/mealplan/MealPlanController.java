package com.sss.app.controller.library.mealplan;

import com.sss.app.dto.library.mealplan.MealPlanCreateRequestDTO;
import com.sss.app.dto.library.mealplan.MealPlanResponseDTO;
import com.sss.app.dto.library.mealplan.MealPlanUpdateRequestDTO;
import com.sss.app.service.library.mealplan.MealPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/meal-plans")
@RequiredArgsConstructor
public class MealPlanController {

    private final MealPlanService mealPlanService;

    @PreAuthorize("@permissionService.hasPermission('library.write')")
    @PostMapping
    public ResponseEntity<MealPlanResponseDTO> create(@Valid @RequestBody MealPlanCreateRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mealPlanService.create(dto));
    }

    @PreAuthorize("@permissionService.hasPermission('library.read')")
    @GetMapping("/{id}")
    public ResponseEntity<MealPlanResponseDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(mealPlanService.getById(id));
    }

    @PreAuthorize("@permissionService.hasPermission('library.read')")
    @GetMapping
    public ResponseEntity<List<MealPlanResponseDTO>> getAll(@RequestParam(required = false) UUID hotelId) {
        return ResponseEntity.ok(mealPlanService.getAll(hotelId));
    }

    @PreAuthorize("@permissionService.hasPermission('library.write')")
    @PutMapping("/{id}")
    public ResponseEntity<MealPlanResponseDTO> update(@PathVariable UUID id,
                                                        @RequestBody MealPlanUpdateRequestDTO dto) {
        return ResponseEntity.ok(mealPlanService.update(id, dto));
    }

    @PreAuthorize("@permissionService.hasPermission('library.write')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        mealPlanService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
