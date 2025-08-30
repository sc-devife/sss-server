package com.sss.app.service.impl;

import com.sss.app.dto.address.AddressDto;
import com.sss.app.dto.organizations.OrganizationsDto;
import com.sss.app.dto.users.UserResponseDto;
import com.sss.app.entity.address.Address;
import com.sss.app.entity.organizations.Organizations;
import com.sss.app.helper.AddressHelper;
import com.sss.app.helper.OrganizationsHelper;
import com.sss.app.mapper.AddressMapper;
import com.sss.app.mapper.OrganizationMapper;
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

    @Override
    public AddressDto createOrganizationAddress(AddressDto createRequest) {
        System.out.println("Create Address Service Impl Started ===");

        Address orgs = addressHelper.createOrganizationAddress(createRequest);
        return addressMapper.mapToDTO(orgs);
    }

    @Override
    public AddressDto updateOrganizationAddress(String addressId, AddressDto createRequest) {
        Address orgs = addressHelper.updateOrganizationAddress(addressId, createRequest);

        System.out.println("Calling Update orgs ==" + orgs );
        return addressMapper.mapToDTO(orgs);
    }

    @Override
    public List<AddressDto> getAddressesByOrganization(Long orgId) {
        return addressRepository.findByOrganizationSeqp(orgId)
                .stream()
                .map(addressMapper::mapToDTO)
                .collect(Collectors.toList());
    }

}
