package com.sss.app.service;

import com.sss.app.dto.BankAccountDto;
import com.sss.app.entity.OrganizationBankDetails;

public interface BankAccountService {

   // List<OrganizationBankDetails> getAccountsForOrg(Long uid);
   BankAccountDto createBankAccount(Long orgId, BankAccountDto dto);
}
