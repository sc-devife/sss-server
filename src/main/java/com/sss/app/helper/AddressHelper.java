package com.sss.app.helper;

import com.sss.app.AddressType;
import com.sss.app.dto.address.AddressDto;
import com.sss.app.dto.organizations.OrganizationsDto;
import com.sss.app.entity.address.Address;
import com.sss.app.entity.address.AddressConstraint;
import com.sss.app.entity.organizations.Organizations;
import com.sss.app.entity.users.User;
import com.sss.app.exception.NotFoundException;
import com.sss.app.mapper.AddressMapper;
import com.sss.app.repository.OrganizationRepository;
import com.sss.app.repository.address.AddressConstraintRepository;
import com.sss.app.repository.address.AddressRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AddressHelper {
    private final AddressRepository addressRepository;
    private final OrganizationRepository organizationRepository;
    private final AddressConstraintRepository constraintRepository;
    private final AddressMapper addressMapper;
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public Address createOrganizationAddress(Long orgId, AddressDto dto) {
        System.out.println("Create Address Helper Started Id ===" + orgId);
        Organizations org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));
        System.out.println("Create Address Helper org === "+ org);

        Address address = Address.create(dto);

        Address savedAddress = addressRepository.save(address);
        addressRepository.flush();
        entityManager.refresh(address);

        // Create AddressConstraints for each role
        for (AddressType type : dto.getAddressTypes()) {

            AddressConstraint constraint = AddressConstraint.create(org, savedAddress, type);
            constraintRepository.save(constraint);
            entityManager.refresh(constraint);
        }
        dto.setId(savedAddress.getSeqp());
        return address;
    }

    @Transactional
    public Address updateOrganizationAddress(Long orgId, Long addressId, AddressDto dto) {
        System.out.println("Calling Update Helper ==" + addressId );
        Organizations org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        address.update(dto);
        // update constraints
        if (dto.getAddressTypes() != null) {
            address.getConstraints().clear();
            for (AddressType type : dto.getAddressTypes()) {
                AddressConstraint constraint = AddressConstraint.create(org, address, type);
              //  AddressConstraint constraint = new AddressConstraint();
                //constraint.setAddress(address);      // owning side
                //constraint.setOrganization(org);     // owning side
                //constraint.setAddressType(type);
                address.getConstraints().add(constraint);
            }
        }

        Address saved = addressRepository.save(address);
        System.out.println("Updated Saved ==== ");
       // entityManager.refresh(address);

        return saved;

    }
/*    public Address getAddressesByOrganization(Long orgId) {
        return addressRepository.findByOrganizationId(orgId)
                .orElseThrow(() -> new NotFoundException("Address not found with Organization id: " + orgId));
    }*/
}
