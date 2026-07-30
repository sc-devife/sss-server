package com.sss.app.helper;

import com.sss.app.AddressType;
import com.sss.app.dto.address.AddressDto;
import com.sss.app.entity.address.Address;
import com.sss.app.entity.address.AddressConstraint;
import com.sss.app.entity.organizations.Organizations;
import com.sss.app.exception.NotFoundException;
import com.sss.app.repository.OrganizationRepository;
import com.sss.app.repository.address.AddressConstraintRepository;
import com.sss.app.repository.address.AddressRepository;
import com.sss.app.security.OrgAccessGuard;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AddressHelper {
    private final AddressRepository addressRepository;
    private final OrganizationRepository organizationRepository;
    private final AddressConstraintRepository constraintRepository;
    private final OrgAccessGuard orgAccessGuard;
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public Address createOrganizationAddress(Long orgId, AddressDto dto) {
        orgAccessGuard.requireAccessToOrg(orgId);
        Organizations org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new NotFoundException("Organization not found"));

        Address address = Address.create(dto);
        address.setOrganizationId(orgId);

        Address savedAddress = addressRepository.save(address);
        addressRepository.flush();
        entityManager.refresh(savedAddress);

        for (AddressType type : addressTypesOrDefault(dto)) {
            if (Boolean.TRUE.equals(dto.getPrimaryAddress())) {
                // remove any old default of same type for this org
                constraintRepository.clearDefaultForOrgAndType(orgId, type);
            }
            AddressConstraint constraint = AddressConstraint.create(org, savedAddress, type, Boolean.TRUE.equals(dto.getPrimaryAddress()));
            constraintRepository.save(constraint);
        }
        return savedAddress;
    }

    @Transactional
    public Address updateOrganizationAddress(Long orgId, Long addressId, AddressDto dto) {
        orgAccessGuard.requireAccessToOrg(orgId);
        Organizations org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new NotFoundException("Organization not found"));

        Address address = getOwnedAddress(orgId, addressId);

        address.update(dto);
        // update constraints
        if (dto.getAddressTypes() != null) {
            address.getConstraints().clear();
            for (AddressType type : addressTypesOrDefault(dto)) {
                if (Boolean.TRUE.equals(dto.getPrimaryAddress())) {
                    constraintRepository.clearDefaultForOrgAndType(orgId, type);
                }
                AddressConstraint constraint = AddressConstraint.create(org, address, type, Boolean.TRUE.equals(dto.getPrimaryAddress()));
                address.getConstraints().add(constraint);
            }
        }

        return addressRepository.save(address);
    }

    @Transactional
    public void deleteOrganizationAddress(Long orgId, Long addressId) {
        orgAccessGuard.requireAccessToOrg(orgId);
        getOwnedAddress(orgId, addressId);
        addressRepository.deleteById(addressId);
    }

    public List<Address> getAddressesForOrg(Long orgId) {
        orgAccessGuard.requireAccessToOrg(orgId);
        return constraintRepository.findByOrganizationSeqp(orgId).stream()
                .map(AddressConstraint::getAddress)
                .distinct()
                .toList();
    }

    private Address getOwnedAddress(Long orgId, Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new NotFoundException("Address not found"));
        boolean belongsToOrg = address.getConstraints().stream()
                .anyMatch(c -> c.getOrganization().getSeqp().equals(orgId));
        if (!belongsToOrg) {
            throw new NotFoundException("Address not found");
        }
        return address;
    }

    private List<AddressType> addressTypesOrDefault(AddressDto dto) {
        return dto.getAddressTypes() == null || dto.getAddressTypes().isEmpty()
                ? List.of(AddressType.CONTACT)
                : dto.getAddressTypes();
    }
}
