package com.sss.app.controller.dashboard;

import com.sss.app.dto.dashboard.DashboardResponseDTO;
import com.sss.app.service.dashboard.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// No @PreAuthorize permission gate — every authenticated user gets their own
// dashboard (Section 4), same as /users/me. Org-wide metrics within the
// response are gated separately (organizations.read) inside the service.
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<DashboardResponseDTO> getDashboard() {
        return ResponseEntity.ok(dashboardService.getDashboard());
    }
}
