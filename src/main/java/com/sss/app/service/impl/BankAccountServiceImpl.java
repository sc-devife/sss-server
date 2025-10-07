package com.sss.app.service.impl;

import com.sss.app.dto.BankAccountDto;
import com.sss.app.entity.OrganizationBankDetails;
import com.sss.app.entity.organizations.Organizations;
import com.sss.app.repository.BankAccountRepository;
import com.sss.app.repository.OrganizationRepository;
import com.sss.app.service.BankAccountService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BankAccountServiceImpl implements BankAccountService {

    private final BankAccountRepository bankAccountRepository;
    private final OrganizationRepository organizationRepository;

    @Transactional
   // public OrganizationBankDetails createBankAccount(Long orgId, BankAccountDto dto) {
    public BankAccountDto createBankAccount(Long orgId, BankAccountDto dto) {
        Organizations org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        // map DTO → Entity
        OrganizationBankDetails account = OrganizationBankDetails.builder()
                .bankName(dto.getBankName())
                .bankShortName(dto.getBankShortName())
                .branchName(dto.getBranchName())
                .ifsc(dto.getIfsc())
                .swiftCode(dto.getSwiftCode())
                .micrCode(dto.getMicrCode())
                .country(dto.getCountry())
                .branchState(dto.getBranchState())
                .branchCity(dto.getBranchCity())
                .branchAddress(dto.getBranchAddress())
                .accountNumber(dto.getAccountNumber())
                .accountName(dto.getAccountName())
                .currency(dto.getCurrency())
                .seqaType("Organization")
                .organization(org)
                .build();
        OrganizationBankDetails saved = bankAccountRepository.save(account);

        // map back Entity → DTO
        return BankAccountDto.builder()
                .id(saved.getSeqp())
                .bankName(saved.getBankName())
                .bankShortName(saved.getBankShortName())
                .branchName(saved.getBranchName())
                .ifsc(saved.getIfsc())
                .swiftCode(saved.getSwiftCode())
                .micrCode(saved.getMicrCode())
                .country(saved.getCountry())
                .branchState(saved.getBranchState())
                .branchCity(saved.getBranchCity())
                .branchAddress(saved.getBranchAddress())
                .accountNumber(saved.getAccountNumber())
                .accountName(saved.getAccountName())
                .currency(saved.getCurrency())
             //   .isPrimary(saved.getIsPrimary())
                .build();    }
}
