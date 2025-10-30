package com.sss.app.helper;

import com.sss.app.dto.BankAccountDto;
import com.sss.app.entity.OrganizationBankDetails;
import com.sss.app.entity.address.Address;
import com.sss.app.entity.organizations.Organizations;
import com.sss.app.repository.BankAccountRepository;
import com.sss.app.repository.OrganizationRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BankAccountsHelper {

    private final BankAccountRepository bankAccountRepository;
    private final OrganizationRepository organizationRepository;
    @PersistenceContext
    private EntityManager entityManager;
    @Transactional
    public OrganizationBankDetails createBankAccount(Long orgId, BankAccountDto dto) {
        Organizations org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        OrganizationBankDetails bankAccount = OrganizationBankDetails.create(dto, org);
        bankAccount  = bankAccountRepository.save(bankAccount);
        bankAccountRepository.flush();
        entityManager.refresh(bankAccount);
        return bankAccount;
    }

   public List<BankAccountDto> getAccountsForOrg(Long orgId) {
 //   public OrganizationBankDetails getAccountsForOrg(Long orgId) {
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

}
