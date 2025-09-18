package com.sss.app.controller;

import com.sss.app.dto.system.CurrencyResponseDto;
import com.sss.app.service.SystemService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/system")
public class SystemController {

    private final SystemService systemService;

    public SystemController(SystemService systemService) {
        this.systemService = systemService;
    }

    @RequestMapping(value = "/currencies", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CurrencyResponseDto>> getSupportedCurrencies() {
        return ResponseEntity.ok(systemService.getSupportedCurrencies());
    }
}
