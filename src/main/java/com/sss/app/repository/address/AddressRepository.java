package com.sss.app.repository.address;
import com.sss.app.AddressType;
import com.sss.app.entity.address.Address;
import com.sss.app.entity.users.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long>{
    Optional<Address> findByUid(String uid);
   // List<Address> findByOrganizationSeqp(Long organizationId);
   // Address findByOrganizationIdAndAddressType(Long organizationId, AddressType addressType);
  //  Address findByOrganizationSeqpAndAddressType(Long organizationId, AddressType addressType);
}
