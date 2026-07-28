package com.sss.app.controller;

import com.sss.app.dto.BankAccountDto;
import com.sss.app.service.BankAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bank-accounts")
@RequiredArgsConstructor
public class BankAccountController {

    private final BankAccountService bankAccountService;

    @PreAuthorize("@permissionService.hasPermission('bank_accounts.write')")
    @PostMapping(value = "/{orgId}/create", consumes = "application/json", produces = "application/json")
    public ResponseEntity<BankAccountDto> createBankAccount(
            @PathVariable Long orgId,
            @RequestBody BankAccountDto dto) {

        BankAccountDto saved = bankAccountService.createBankAccount(orgId, dto);

        dto.setId(saved.getId());
        return ResponseEntity.ok(dto);

    }

    @PreAuthorize("@permissionService.hasPermission('bank_accounts.read')")
    @GetMapping("/{orgId}")
    public ResponseEntity<List<BankAccountDto>> getBankAccounts(@PathVariable Long orgId) {
        List<BankAccountDto> accounts = bankAccountService.getAccountsForOrg(orgId);
        return ResponseEntity.ok(accounts);
    }

    @PreAuthorize("@permissionService.hasPermission('bank_accounts.write')")
    @PatchMapping("/{orgId}/{accountId}/deactivate")
    public ResponseEntity<BankAccountDto> deactivateBankAccount(@PathVariable Long orgId,
                                                                 @PathVariable Long accountId) {
        return ResponseEntity.ok(bankAccountService.deactivateBankAccount(orgId, accountId));
    }

    @PreAuthorize("@permissionService.hasPermission('bank_accounts.write')")
    @PatchMapping("/{orgId}/{accountId}/reactivate")
    public ResponseEntity<BankAccountDto> reactivateBankAccount(@PathVariable Long orgId,
                                                                 @PathVariable Long accountId) {
        return ResponseEntity.ok(bankAccountService.reactivateBankAccount(orgId, accountId));
    }
}
