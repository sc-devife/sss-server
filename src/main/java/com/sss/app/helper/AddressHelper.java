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
        Organizations org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        Address address = Address.create(dto);

        Address savedAddress = addressRepository.save(address);
        addressRepository.flush();
        entityManager.refresh(address);

        for (AddressType type : dto.getAddressTypes()) {
            if (Boolean.TRUE.equals(dto.getPrimaryAddress())) {
                // remove any old default of same type for this org
                constraintRepository.clearDefaultForOrgAndType(orgId, type);
            }
            AddressConstraint constraint = AddressConstraint.create(org, savedAddress, type,dto.getPrimaryAddress());
            constraintRepository.save(constraint);
            entityManager.refresh(constraint);
        }
        dto.setId(savedAddress.getSeqp());
        return address;
    }

    @Transactional
    public Address updateOrganizationAddress(Long orgId, Long addressId, AddressDto dto) {
        Organizations org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        address.update(dto);
        // update constraints
        if (dto.getAddressTypes() != null) {
            address.getConstraints().clear();
            for (AddressType type : dto.getAddressTypes()) {
                if (Boolean.TRUE.equals(dto.getPrimaryAddress())) {
                    constraintRepository.clearDefaultForOrgAndType(orgId, type);
                }
                AddressConstraint constraint = AddressConstraint.create(org, address, type, dto.getPrimaryAddress());
                address.getConstraints().add(constraint);
            }
        }

        Address saved = addressRepository.save(address);
        return saved;
    }

    @Transactional
    public void deleteOrganizationAddress(Long orgId, Long addressId) {
        // verify the address belongs to the organization
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        if (!address.getConstraints().stream()
                .anyMatch(c -> c.getOrganization().getSeqp().equals(orgId))) {
            throw new RuntimeException("Address does not belong to this organization");
        }

        // 1️⃣ remove constraints first (to avoid FK violations)
       // constraintRepository.deleteByAddressId(addressId);

        // 2️⃣ then delete the address
        addressRepository.deleteById(addressId);
    }
}
