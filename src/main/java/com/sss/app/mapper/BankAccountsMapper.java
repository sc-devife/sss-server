package com.sss.app.mapper;

import com.sss.app.dto.BankAccountDto;
import com.sss.app.dto.address.AddressDto;
import com.sss.app.entity.OrganizationBankDetails;
import com.sss.app.entity.address.Address;
import org.springframework.stereotype.Component;

@Component
public class BankAccountsMapper {

    public BankAccountDto mapToDTO(OrganizationBankDetails accounts) {
        return BankAccountDto.builder()
                .id(accounts.getSeqp())
                .bankName(accounts.getBankName())
                .bankShortName(accounts.getBankShortName())
                .branchName(accounts.getBranchName())
                .ifsc(accounts.getIfsc())
                .swiftCode(accounts.getSwiftCode())
                .micrCode(accounts.getMicrCode())
                .country(accounts.getCountry())
                .branchState(accounts.getBranchState())
                .branchCity(accounts.getBranchCity())
                .branchAddress(accounts.getBranchAddress())
                .accountNumber(accounts.getAccountNumber())
                .accountName(accounts.getAccountName())
                .currency(accounts.getCurrency())
               // .isPrimary(accounts.getIsPrimary())
                .build();
    }
}
