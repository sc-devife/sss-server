package com.sss.app.service.impl;

import com.sss.app.dto.BankAccountDto;
import com.sss.app.helper.BankAccountsHelper;
import com.sss.app.mapper.BankAccountsMapper;
import com.sss.app.service.BankAccountService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BankAccountServiceImpl implements BankAccountService {

    private final BankAccountsHelper accountsHelper;
    private final BankAccountsMapper accountsMapper;

    @Override
    public BankAccountDto createBankAccount(String orgUid, BankAccountDto createRequest) {
        return accountsMapper.mapToDTO(accountsHelper.createBankAccount(orgUid, createRequest));
    }

    @Override
    public List<BankAccountDto> getAccountsForOrg(String orgUid) {
        return accountsHelper.getAccountsForOrg(orgUid);
    }

    @Transactional
    @Override
    public BankAccountDto deactivateBankAccount(String orgUid, UUID accountUid) {
        return accountsMapper.mapToDTO(accountsHelper.deactivateBankAccount(orgUid, accountUid));
    }

    @Transactional
    @Override
    public BankAccountDto reactivateBankAccount(String orgUid, UUID accountUid) {
        return accountsMapper.mapToDTO(accountsHelper.reactivateBankAccount(orgUid, accountUid));
    }
}
