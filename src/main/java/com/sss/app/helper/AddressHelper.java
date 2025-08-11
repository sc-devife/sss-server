package com.sss.app.helper;

import com.sss.app.dto.address.AddressDto;
import com.sss.app.dto.organizations.OrganizationsDto;
import com.sss.app.entity.address.Address;
import com.sss.app.entity.organizations.Organizations;
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

    public Address createOrganizationAddress(AddressDto dto) {
        Organizations org = organizationRepository.findById(dto.getOrganizationId())
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        boolean exists = addressRepository.findByOrganizationSeqpAndAddressType(
                dto.getOrganizationId(), dto.getAddressType()) != null;

        if (exists) {
            throw new RuntimeException("Address of type " + dto.getAddressType() + " already exists for this organization");
        }

        //To-Do
        Address address = Address.builder()
                .street(dto.getStreet())
                .city(dto.getCity())
                .state(dto.getState())
                .zipCode(dto.getZipCode())
                .country(dto.getCountry())
                .addressType(dto.getAddressType())
                .organization(org)
                .build();
        entityManager.flush();
        entityManager.refresh(org);
        return address;

//        return addressMapper.mapToDTO(addressRepository.save(address));
    }
    public Address updateOrganizationAddress(Long addressId, AddressDto dto) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        address.setStreet(dto.getStreet());
        address.setCity(dto.getCity());
        address.setState(dto.getState());
        address.setZipCode(dto.getZipCode());
        address.setCountry(dto.getCountry());
        return address;

    }
/*    public Address getAddressesByOrganization(Long orgId) {
        return addressRepository.findByOrganizationId(orgId)
                .orElseThrow(() -> new NotFoundException("Address not found with Organization id: " + orgId));
    }*/
}
