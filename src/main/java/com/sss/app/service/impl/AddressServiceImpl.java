package com.sss.app.service.impl;

import com.sss.app.dto.address.AddressDto;
import com.sss.app.entity.address.Address;
import com.sss.app.helper.AddressHelper;
import com.sss.app.mapper.AddressMapper;
import com.sss.app.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {
    private final AddressHelper addressHelper;
    private final AddressMapper addressMapper;

    @Override
    public AddressDto createOrganizationAddress(Long orgId, AddressDto createRequest) {
        return addressMapper.mapToDTO(addressHelper.createOrganizationAddress(orgId, createRequest));
    }

    @Override
    public AddressDto updateOrganizationAddress(Long orgId, Long addressId, AddressDto createRequest) {
        return addressMapper.mapToDTO(addressHelper.updateOrganizationAddress(orgId, addressId, createRequest));
    }

    @Override
    public List<AddressDto> getAddressesForOrg(Long orgId) {
        return addressHelper.getAddressesForOrg(orgId).stream().map(addressMapper::mapToDTO).toList();
    }

    @Override
    public void deleteOrganizationAddress(Long orgId, Long addressId) {
        addressHelper.deleteOrganizationAddress(orgId, addressId);
    }
}
