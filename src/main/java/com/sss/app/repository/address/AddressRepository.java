package com.sss.app.repository.address;
import com.sss.app.AddressType;
import com.sss.app.entity.address.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
public interface AddressRepository extends JpaRepository<Address, Long>{

    List<Address> findByOrganizationSeqp(Long organizationId);
   // Address findByOrganizationIdAndAddressType(Long organizationId, AddressType addressType);
    Address findByOrganizationSeqpAndAddressType(Long organizationId, AddressType addressType);
}
