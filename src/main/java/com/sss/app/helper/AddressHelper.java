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
    public Address createOrganizationAddress(String orgUid, AddressDto dto) {
        Organizations org = resolveOrg(orgUid);
        orgAccessGuard.requireAccessToOrg(org.getSeqp());

        Address address = Address.create(dto);
        address.setOrganizationId(org.getSeqp());

        Address savedAddress = addressRepository.save(address);
        addressRepository.flush();
        entityManager.refresh(savedAddress);

        for (AddressType type : addressTypesOrDefault(dto)) {
            if (Boolean.TRUE.equals(dto.getPrimaryAddress())) {
                // remove any old default of same type for this org
                constraintRepository.clearDefaultForOrgAndType(org.getSeqp(), type);
            }
            AddressConstraint constraint = AddressConstraint.create(org, savedAddress, type, Boolean.TRUE.equals(dto.getPrimaryAddress()));
            constraintRepository.save(constraint);
        }
        return savedAddress;
    }

    @Transactional
    public Address updateOrganizationAddress(String orgUid, String addressUid, AddressDto dto) {
        Organizations org = resolveOrg(orgUid);
        orgAccessGuard.requireAccessToOrg(org.getSeqp());

        Address address = getOwnedAddress(org, addressUid);

        address.update(dto);
        // update constraints
        if (dto.getAddressTypes() != null) {
            address.getConstraints().clear();
            for (AddressType type : addressTypesOrDefault(dto)) {
                if (Boolean.TRUE.equals(dto.getPrimaryAddress())) {
                    constraintRepository.clearDefaultForOrgAndType(org.getSeqp(), type);
                }
                AddressConstraint constraint = AddressConstraint.create(org, address, type, Boolean.TRUE.equals(dto.getPrimaryAddress()));
                address.getConstraints().add(constraint);
            }
        }

        return addressRepository.save(address);
    }

    @Transactional
    public void deleteOrganizationAddress(String orgUid, String addressUid) {
        Organizations org = resolveOrg(orgUid);
        orgAccessGuard.requireAccessToOrg(org.getSeqp());
        Address address = getOwnedAddress(org, addressUid);
        addressRepository.delete(address);
    }

    public List<Address> getAddressesForOrg(String orgUid) {
        Organizations org = resolveOrg(orgUid);
        orgAccessGuard.requireAccessToOrg(org.getSeqp());
        return constraintRepository.findByOrganizationSeqp(org.getSeqp()).stream()
                .map(AddressConstraint::getAddress)
                .distinct()
                .toList();
    }

    private Organizations resolveOrg(String orgUid) {
        return organizationRepository.findByUid(orgUid)
                .orElseThrow(() -> new NotFoundException("Organization not found"));
    }

    private Address getOwnedAddress(Organizations org, String addressUid) {
        Address address = addressRepository.findByUid(addressUid)
                .orElseThrow(() -> new NotFoundException("Address not found"));
        boolean belongsToOrg = address.getConstraints().stream()
                .anyMatch(c -> c.getOrganization().getSeqp().equals(org.getSeqp()));
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
