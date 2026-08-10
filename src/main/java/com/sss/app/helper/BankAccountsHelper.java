package com.sss.app.helper;

import com.sss.app.dto.BankAccountDto;
import com.sss.app.entity.OrganizationBankDetails;
import com.sss.app.entity.address.Address;
import com.sss.app.entity.organizations.Organizations;
import com.sss.app.exception.NotFoundException;
import com.sss.app.repository.BankAccountRepository;
import com.sss.app.repository.OrganizationRepository;
import com.sss.app.security.OrgAccessGuard;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BankAccountsHelper {

    private final BankAccountRepository bankAccountRepository;
    private final OrganizationRepository organizationRepository;
    private final OrgAccessGuard orgAccessGuard;
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public OrganizationBankDetails createBankAccount(String orgUid, BankAccountDto dto) {
        Organizations org = resolveOrg(orgUid);
        orgAccessGuard.requireAccessToOrg(org.getSeqp());

        OrganizationBankDetails bankAccount = OrganizationBankDetails.create(dto, org);
        bankAccount  = bankAccountRepository.save(bankAccount);
        bankAccountRepository.flush();
        entityManager.refresh(bankAccount);
        return bankAccount;
    }

   public List<BankAccountDto> getAccountsForOrg(String orgUid) {
        Organizations org = resolveOrg(orgUid);
        orgAccessGuard.requireAccessToOrg(org.getSeqp());
        List<OrganizationBankDetails> accounts = bankAccountRepository.findByOrganizationSeqp(org.getSeqp());

        return accounts.stream()
                .map(acc -> BankAccountDto.builder()
                        .uid(acc.getUid())
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
                        .status(acc.getStatus())
                        //  .isPrimary(acc.getIsPrimary())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public OrganizationBankDetails deactivateBankAccount(String orgUid, UUID accountUid) {
        OrganizationBankDetails account = getOwnedAccount(orgUid, accountUid);
        account.setStatus("inactive");
        return bankAccountRepository.save(account);
    }

    @Transactional
    public OrganizationBankDetails reactivateBankAccount(String orgUid, UUID accountUid) {
        OrganizationBankDetails account = getOwnedAccount(orgUid, accountUid);
        account.setStatus("active");
        return bankAccountRepository.save(account);
    }

    private Organizations resolveOrg(String orgUid) {
        return organizationRepository.findByUid(orgUid)
                .orElseThrow(() -> new NotFoundException("Organization not found"));
    }

    private OrganizationBankDetails getOwnedAccount(String orgUid, UUID accountUid) {
        Organizations org = resolveOrg(orgUid);
        orgAccessGuard.requireAccessToOrg(org.getSeqp());

        OrganizationBankDetails account = bankAccountRepository.findByUid(accountUid)
                .orElseThrow(() -> new RuntimeException("Bank account not found"));

        if (!account.getOrganization().getSeqp().equals(org.getSeqp())) {
            throw new RuntimeException("Bank account does not belong to this organization");
        }
        return account;
    }

}
