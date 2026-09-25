package com.sss.app.controller.exchangerate;

import com.sss.app.dto.exchangerate.ExchangeRateResponseDTO;
import com.sss.app.dto.exchangerate.OrgExchangeRateRowDTO;
import com.sss.app.dto.exchangerate.OrgExchangeRateUpdateRequestDTO;
import com.sss.app.service.exchangerate.ExchangeRateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/exchange-rates")
@RequiredArgsConstructor
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    // The rate that applies to a pair for the caller's vendor (used when building quotes).
    @PreAuthorize("@permissionService.hasPermission('trips.read')")
    @GetMapping
    public ResponseEntity<ExchangeRateResponseDTO> resolve(@RequestParam String from, @RequestParam String to) {
        return ResponseEntity.ok(exchangeRateService.resolve(from, to));
    }

    // The vendor's rate table: base currency against every other currency.
    @PreAuthorize("@permissionService.hasPermission('organizations.read')")
    @GetMapping("/org")
    public ResponseEntity<List<OrgExchangeRateRowDTO>> listForOrg() {
        return ResponseEntity.ok(exchangeRateService.listForCurrentOrg());
    }

    @PreAuthorize("@permissionService.hasPermission('organizations.write')")
    @PutMapping("/org/{to}")
    public ResponseEntity<OrgExchangeRateRowDTO> setOrgRate(@PathVariable String to,
                                                            @Valid @RequestBody OrgExchangeRateUpdateRequestDTO request) {
        return ResponseEntity.ok(exchangeRateService.setOrgRate(to, request));
    }

    @PreAuthorize("@permissionService.hasPermission('organizations.write')")
    @DeleteMapping("/org/{to}")
    public ResponseEntity<OrgExchangeRateRowDTO> resetOrgRate(@PathVariable String to) {
        return ResponseEntity.ok(exchangeRateService.resetOrgRate(to));
    }

    // Fetch today's market rates right now instead of waiting for the daily job.
    @PreAuthorize("@permissionService.hasPermission('organizations.write')")
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Integer>> refresh() {
        return ResponseEntity.ok(Map.of("stored", exchangeRateService.refreshMarketRates()));
    }
}
