package com.sss.app.service;

import com.sss.app.dto.address.AddressDto;
import com.sss.app.dto.organizations.OrganizationsDto;

import java.util.List;

public interface AddressService {
  List<AddressDto> getAddressesByOrganization(Long orgId);
    AddressDto createOrganizationAddress(AddressDto createRequest);
    AddressDto updateOrganizationAddress(Long addressId, AddressDto createRequest);
}
