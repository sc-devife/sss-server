package com.sss.app.repository.address;

import com.sss.app.AddressType;
import com.sss.app.entity.address.AddressConstraint;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AddressConstraintRepository extends JpaRepository<AddressConstraint, Long> {
        List<AddressConstraint> findByOrganizationUid(String orgId);
        @Modifying
        @Query("update AddressConstraint c set c.primaryAddress = false " +
                "where c.organization.seqp = :orgId and c.addressType = :type and c.primaryAddress = true")
        void clearDefaultForOrgAndType(@Param("orgId") Long orgId,
                                       @Param("type") AddressType type);

}
