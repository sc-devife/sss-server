package com.sss.app.repository;

import com.sss.app.dto.BankAccountDto;
import com.sss.app.entity.OrganizationBankDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BankAccountRepository  extends JpaRepository<OrganizationBankDetails, Long> {
   List<OrganizationBankDetails> findByOrganizationSeqp(Long orgId);
   Optional<OrganizationBankDetails> findByUid(UUID uid);

   @Modifying
   @Query("update OrganizationBankDetails b set b.isDefault = false where b.organization.seqp = :orgId")
   void clearDefaultForOrg(Long orgId);
}
