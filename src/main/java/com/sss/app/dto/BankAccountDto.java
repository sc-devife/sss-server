package com.sss.app.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankAccountDto {
    private UUID uid;
    private String bankName;
    private String bankShortName;
    private String branchName;
    private String ifsc;
    private String swiftCode;
    private String micrCode;
    private String country;
    private String branchState;
    private String branchCity;
    private String branchAddress;
    private String accountNumber;
    private String accountName;
    private String currency;
    private String status;
    private Boolean isDefault;
}
