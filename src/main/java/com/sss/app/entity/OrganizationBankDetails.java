package com.sss.app.entity;

import com.sss.app.entity.organizations.Organizations;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "organization_bank_accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationBankDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seqp;

    // ---- Bank Details ----
    @Column(name = "bank_name", nullable = false)
    private String bankName;

    @Column(name = "bank_short_name", nullable = false)
    private String bankShortName;

    // ---- Branch Details ----
    @Column(name = "branch_name", nullable = false)
    private String branchName;

    @Column(name = "ifsc", nullable = false)
    private String ifsc;

    @Column(name = "swift_code")
    private String swiftCode;

    @Column(name = "micr_code")
    private String micrCode;

    @Column(name = "branch_country", nullable = false)
    private String country;

    @Column(name = "branch_state", nullable = false)
    private String branchState;

    @Column(name = "branch_city", nullable = false)
    private String branchCity;

    @Column(name = "branch_address")
    private String branchAddress;

    // ---- Account Details ----
    @Column(name = "account_number", nullable = false)
    private String accountNumber;

    @Column(name = "account_name", nullable = false)
    private String accountName;

    @Column(name = "currency", nullable = false)
    private String currency;

    // Organization link
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seqa", nullable = false)
    private Organizations organization;

    @Column(name = "seqa_type", nullable = false) // Just a plain column
    private String seqaType;

}
