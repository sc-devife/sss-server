package com.sss.app.controller;

import com.sss.app.dto.BankAccountDto;
import com.sss.app.service.BankAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/bank-accounts")
@RequiredArgsConstructor
public class BankAccountController {

    private final BankAccountService bankAccountService;

    @PreAuthorize("@permissionService.hasPermission('bank_accounts.write')")
    @PostMapping(value = "/{orgId}/create", consumes = "application/json", produces = "application/json")
    public ResponseEntity<BankAccountDto> createBankAccount(
            @PathVariable String orgId,
            @RequestBody BankAccountDto dto) {

        BankAccountDto saved = bankAccountService.createBankAccount(orgId, dto);

        dto.setUid(saved.getUid());
        return ResponseEntity.ok(dto);

    }

    @PreAuthorize("@permissionService.hasPermission('bank_accounts.read')")
    @GetMapping("/{orgId}")
    public ResponseEntity<List<BankAccountDto>> getBankAccounts(@PathVariable String orgId) {
        List<BankAccountDto> accounts = bankAccountService.getAccountsForOrg(orgId);
        return ResponseEntity.ok(accounts);
    }

    @PreAuthorize("@permissionService.hasPermission('bank_accounts.write')")
    @PatchMapping("/{orgId}/{accountId}/deactivate")
    public ResponseEntity<BankAccountDto> deactivateBankAccount(@PathVariable String orgId,
                                                                 @PathVariable UUID accountId) {
        return ResponseEntity.ok(bankAccountService.deactivateBankAccount(orgId, accountId));
    }

    @PreAuthorize("@permissionService.hasPermission('bank_accounts.write')")
    @PatchMapping("/{orgId}/{accountId}/reactivate")
    public ResponseEntity<BankAccountDto> reactivateBankAccount(@PathVariable String orgId,
                                                                 @PathVariable UUID accountId) {
        return ResponseEntity.ok(bankAccountService.reactivateBankAccount(orgId, accountId));
    }
}
