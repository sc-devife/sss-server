package com.sss.app.repository;

import com.sss.app.dto.BankAccountDto;
import com.sss.app.entity.OrganizationBankDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BankAccountRepository  extends JpaRepository<OrganizationBankDetails, Long> {
   List<OrganizationBankDetails> findByOrganizationSeqp(Long orgId);
}
