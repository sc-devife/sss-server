package com.sss.app.repository.address;

import com.sss.app.entity.address.AddressConstraint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AddressConstraintRepository extends JpaRepository<AddressConstraint, Long> {
        List<AddressConstraint> findByOrganizationUid(String orgId);

}
