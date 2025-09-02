package com.sss.app.service;

import com.sss.app.dto.address.AddressDto;
import com.sss.app.dto.organizations.OrganizationsDto;

import java.util.List;

public interface AddressService {
  List<AddressDto> getAddressesByOrganization(String uid);
   // AddressDto createOrganizationAddress(AddressDto createRequest);
    AddressDto updateOrganizationAddress(Long orgId, Long AddressId, AddressDto updateRequest);

  AddressDto createOrganizationAddress(Long orgId, AddressDto dto);
}
