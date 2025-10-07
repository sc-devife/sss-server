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

  /*  @GetMapping
    public List<BankAccountDto> listBankAccounts(@PathVariable Long orgId) {
      //  List<OrganizationBankDetails> accounts = bankAccountService.getAccountsForOrg(orgId);
        List<OrganizationBankDetails> accounts = bankAccountService.getAccountsForOrg(orgId);

        List<BankAccountDto> dtos = new ArrayList<>();

        for (OrganizationBankDetails acc : accounts) {
            BankAccountDto dto = BankAccountDto.builder()
                    .id(acc.getId())
                    .bankName(acc.getBankName())
                    .bankShortName(acc.getBankShortName())
                    .branchName(acc.getBranchName())
                    .ifsc(acc.getIfsc())
                    .swiftCode(acc.getSwiftCode())
                    .micrCode(acc.getMicrCode())
                    .country(acc.getCountry())
                    .branchState(acc.getBranchState())
                    .branchCity(acc.getBranchCity())
                    .branchAddress(acc.getBranchAddress())
                    .accountNumber(acc.getAccountNumber())
                    .accountName(acc.getAccountName())
                    .currency(acc.getCurrency())
                    .build();

            dtos.add(dto);
        }

        return dtos;
    }

    @GetMapping
    public List<BankAccountDto> listBankAccounts(@PathVariable Long orgId) {
        return bankAccountService.getAccountsForOrg(orgId)
                .stream()
                .map(acc -> BankAccountDto.builder()
                        .id(acc.getId())
                        .bankName(acc.getBankName())
                        .bankShortName(acc.getBankShortName())
                        .branchName(acc.getBranchName())
                        .ifsc(acc.getIfsc())
                        .swiftCode(acc.getSwiftCode())
                        .micrCode(acc.getMicrCode())
                        .country(acc.getCountry())
                        .branchState(acc.getBranchState())
                        .branchCity(acc.getBranchCity())
                        .branchAddress(acc.getBranchAddress())
                        .accountNumber(acc.getAccountNumber())
                        .accountName(acc.getAccountName())
                        .currency(acc.getCurrency())
                        .build())
                .toList();
    }*/
}
