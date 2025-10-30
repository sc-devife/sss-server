package com.sss.app.service.impl;

import com.sss.app.dto.BankAccountDto;
import com.sss.app.dto.address.AddressDto;
import com.sss.app.dto.users.UserResponseDto;
import com.sss.app.entity.OrganizationBankDetails;
import com.sss.app.entity.organizations.Organizations;
import com.sss.app.helper.AddressHelper;
import com.sss.app.helper.BankAccountsHelper;
import com.sss.app.mapper.AddressMapper;
import com.sss.app.mapper.BankAccountsMapper;
import com.sss.app.repository.BankAccountRepository;
import com.sss.app.repository.OrganizationRepository;
import com.sss.app.service.BankAccountService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BankAccountServiceImpl implements BankAccountService {

    private final BankAccountsHelper accountsHelper;
    private final BankAccountsMapper accountsMapper;

    private final BankAccountRepository bankAccountRepository;
    private final OrganizationRepository organizationRepository;

    @Override
    public BankAccountDto createBankAccount(Long orgId, BankAccountDto createRequest) {
        return accountsMapper.mapToDTO(accountsHelper.createBankAccount(orgId, createRequest));
    }

 /*   @Override
    public BankAccountDto getAccountsForOrg(Long orgId) {
        return accountsMapper.mapToDTO(accountsHelper.getAccountsForOrg(orgId));
    }*/

    @Override
    public List<BankAccountDto> getAccountsForOrg(Long orgId) {
        List<OrganizationBankDetails> accounts = bankAccountRepository.findByOrganizationSeqp(orgId);

        return accounts.stream()
                .map(acc -> BankAccountDto.builder()
                        .id(acc.getSeqp())
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
                      //  .isPrimary(acc.getIsPrimary())
                        .build())
                .collect(Collectors.toList());
    }
    @Transactional
    @Override
    public void deleteBankAccount(Long orgId, Long accountId) {
        OrganizationBankDetails account = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Bank account not found"));

        if (!account.getOrganization().getSeqp().equals(orgId)) {
            throw new RuntimeException("Bank account does not belong to this organization");
        }

        bankAccountRepository.delete(account);
    }
}
