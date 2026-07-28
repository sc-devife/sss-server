package com.sss.app.service;

import com.sss.app.dto.BankAccountDto;
import com.sss.app.entity.OrganizationBankDetails;

import java.util.List;

public interface BankAccountService {

   List<BankAccountDto> getAccountsForOrg(Long orgId);
   BankAccountDto createBankAccount(Long orgId, BankAccountDto dto);
   BankAccountDto deactivateBankAccount(Long orgId, Long accountId);

}
