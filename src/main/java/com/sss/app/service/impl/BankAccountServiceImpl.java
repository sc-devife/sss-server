package com.sss.app.service.impl;

import com.sss.app.dto.BankAccountDto;
import com.sss.app.helper.BankAccountsHelper;
import com.sss.app.mapper.BankAccountsMapper;
import com.sss.app.service.BankAccountService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BankAccountServiceImpl implements BankAccountService {

    private final BankAccountsHelper accountsHelper;
    private final BankAccountsMapper accountsMapper;

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
    public BankAccountDto deactivateBankAccount(Long orgId, Long accountId) {
        return accountsMapper.mapToDTO(accountsHelper.deactivateBankAccount(orgId, accountId));
    }

    @Transactional
    @Override
    public BankAccountDto reactivateBankAccount(Long orgId, Long accountId) {
        return accountsMapper.mapToDTO(accountsHelper.reactivateBankAccount(orgId, accountId));
    }
}
