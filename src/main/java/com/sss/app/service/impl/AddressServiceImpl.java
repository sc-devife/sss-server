package com.sss.app.service.impl;

import com.sss.app.dto.address.AddressDto;
import com.sss.app.dto.organizations.OrganizationsDto;
import com.sss.app.dto.users.UserCreateRequestDto;
import com.sss.app.dto.users.UserResponseDto;
import com.sss.app.entity.address.Address;
import com.sss.app.entity.address.AddressConstraint;
import com.sss.app.entity.organizations.Organizations;
import com.sss.app.helper.AddressHelper;
import com.sss.app.helper.OrganizationsHelper;
import com.sss.app.mapper.AddressMapper;
import com.sss.app.mapper.OrganizationMapper;
import com.sss.app.repository.address.AddressConstraintRepository;
import com.sss.app.repository.address.AddressRepository;
import com.sss.app.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {
    private final AddressHelper addressHelper;
    private final AddressMapper addressMapper;
    private final AddressRepository addressRepository;
    private final AddressConstraintRepository constraintRepository;

    @Override
    public AddressDto createOrganizationAddress(Long orgId, AddressDto createRequest) {
        System.out.println("Create Address Service Impl Started ===");

       // Address orgs = addressHelper.createOrganizationAddress(orgId, createRequest);
        return addressMapper.mapToDTO(addressHelper.createOrganizationAddress(orgId, createRequest));
    }

    @Override
    public AddressDto updateOrganizationAddress(Long orgId, Long addressId, AddressDto createRequest) {
        System.out.println("Calling Update Imple ==" + addressId );
        return addressMapper.mapToDTO(addressHelper.updateOrganizationAddress(orgId, addressId, createRequest));
    }
    @Override
    public List<AddressDto> getAddressesByOrganization(String orgId) {

        List<AddressConstraint> constraints = constraintRepository.findByOrganizationUid(orgId);

        return constraints.stream().map(c -> AddressDto.builder()
                .id(c.getAddress().getSeqp())
                .street(c.getAddress().getStreet())
                .city(c.getAddress().getCity())
                .state(c.getAddress().getState())
                .zipCode(c.getAddress().getZipCode())
                .country(c.getAddress().getCountry())
                .addressTypes(
                        constraints.stream()
                                .filter(inner -> inner.getAddress().getSeqp().equals(c.getAddress().getSeqp()))
                                .map(AddressConstraint::getAddressType)
                                .distinct()
                                .toList()
                )
                .build()
        ).distinct().toList();
    }
     /*   return addressRepository.findByOrganizationSeqp(orgId)
                .stream()
                .map(addressMapper::mapToDTO)
                .collect(Collectors.toList());
   */

}
