package com.sss.app.service;

import com.sss.app.dto.BankAccountDto;
import com.sss.app.entity.OrganizationBankDetails;

import java.util.List;
import java.util.UUID;

public interface BankAccountService {

   List<BankAccountDto> getAccountsForOrg(String orgUid);
   BankAccountDto createBankAccount(String orgUid, BankAccountDto dto);
   BankAccountDto deactivateBankAccount(String orgUid, UUID accountUid);
   BankAccountDto reactivateBankAccount(String orgUid, UUID accountUid);
   BankAccountDto setDefaultBankAccount(String orgUid, UUID accountUid);

}
