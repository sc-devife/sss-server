package com.sss.app.service.impl;

import com.sss.app.dto.BankAccountDto;
import com.sss.app.entity.OrganizationBankDetails;
import com.sss.app.helper.BankAccountsHelper;
import com.sss.app.mapper.BankAccountsMapper;
import com.sss.app.repository.BankAccountRepository;
import com.sss.app.security.OrgAccessGuard;
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
    private final OrgAccessGuard orgAccessGuard;

    @Override
    public BankAccountDto createBankAccount(Long orgId, BankAccountDto createRequest) {
        return accountsMapper.mapToDTO(accountsHelper.createBankAccount(orgId, createRequest));
    }

    @Override
    public List<BankAccountDto> getAccountsForOrg(Long orgId) {
        return accountsHelper.getAccountsForOrg(orgId);
    }

    @Transactional
    @Override
    public void deleteBankAccount(Long orgId, Long accountId) {
        orgAccessGuard.requireAccessToOrg(orgId);

        OrganizationBankDetails account = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Bank account not found"));

        if (!account.getOrganization().getSeqp().equals(orgId)) {
            throw new RuntimeException("Bank account does not belong to this organization");
        }

        bankAccountRepository.delete(account);
    }
}
