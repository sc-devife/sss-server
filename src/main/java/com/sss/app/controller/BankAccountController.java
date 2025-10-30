package com.sss.app.controller;

import com.sss.app.dto.BankAccountDto;
import com.sss.app.entity.OrganizationBankDetails;
import com.sss.app.service.BankAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/bank-accounts")
@RequiredArgsConstructor
public class BankAccountController {

    private final BankAccountService bankAccountService;

    @PostMapping(value = "/{orgId}/create", consumes = "application/json", produces = "application/json")
    public ResponseEntity<BankAccountDto> createBankAccount(
            @PathVariable Long orgId,
            @RequestBody BankAccountDto dto) {

        BankAccountDto saved = bankAccountService.createBankAccount(orgId, dto);

        dto.setId(saved.getId());
        return ResponseEntity.ok(dto);

    }

    @RequestMapping("/{orgId}")
    public ResponseEntity<List<BankAccountDto>> getBankAccounts(@PathVariable Long orgId) {
        List<BankAccountDto> accounts = bankAccountService.getAccountsForOrg(orgId);
        return ResponseEntity.ok(accounts);
    }

    @DeleteMapping("/{orgId}/{accountId}")
    public ResponseEntity<Void> deleteBankAccount(@PathVariable Long orgId,
                                                  @PathVariable Long accountId) {
        bankAccountService.deleteBankAccount(orgId, accountId);
        return ResponseEntity.noContent().build(); // returns 204 No Content
    }
}
