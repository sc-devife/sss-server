package com.sss.app.helper;

import com.sss.app.dto.address.AddressDto;
import com.sss.app.dto.organizations.OrganizationsDto;
import com.sss.app.entity.address.Address;
import com.sss.app.entity.organizations.Organizations;
import com.sss.app.entity.users.User;
import com.sss.app.exception.NotFoundException;
import com.sss.app.mapper.AddressMapper;
import com.sss.app.repository.OrganizationRepository;
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
    private final AddressMapper addressMapper;
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public Address createOrganizationAddress(AddressDto dto) {
        System.out.println("Create Address Helper Started Id ===" + dto.getOrganizationId());
        Organizations org = organizationRepository.findById(dto.getOrganizationId())
                .orElseThrow(() -> new RuntimeException("Organization not found"));
        System.out.println("Create Address Helper org === "+ org);

        boolean exists = addressRepository.findByOrganizationSeqpAndAddressType(
                dto.getOrganizationId(), dto.getAddressType()) != null;
        System.out.println("Create Address Helper exists === "+ exists);

        if (exists) {
            throw new RuntimeException("Address of type " + dto.getAddressType() + " already exists for this organization");
        }

        Address address = Address.create(dto, org);
        address = addressRepository.save(address);
        entityManager.flush();
        entityManager.refresh(address);
        return address;

//        return addressMapper.mapToDTO(addressRepository.save(address));
    }
    public Address getAddressByUid(String uid) {
        return addressRepository.findByUid(uid).orElseThrow(() -> new NotFoundException("Address not found with uid: " + uid));
    }
    @Transactional
    public Address updateOrganizationAddress(String addressId, AddressDto dto) {
        System.out.println("Calling Update Helper ==" + addressId );

        Address address = getAddressByUid(addressId);
        System.out.println("updateUser user - "+ address);
        address.update(dto);
        System.out.println("updateUser updated user - "+ address);
        addressRepository.save(address);
/*
        Address address = addressRepository.findByUid(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found"));
        address.setStreet(dto.getStreet());
        address.setCity(dto.getCity());
        address.setState(dto.getState());
        address.setZipCode(dto.getZipCode());
        address.setCountry(dto.getCountry());

        address = addressRepository.save(address);*/
        entityManager.flush();
        entityManager.refresh(address);

        return address;

    }
}
