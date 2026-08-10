package com.sss.app.service;

import com.sss.app.dto.address.AddressDto;

import java.util.List;

public interface AddressService {
  List<AddressDto> getAddressesForOrg(String orgId);
  AddressDto updateOrganizationAddress(String orgId, String addressId, AddressDto updateRequest);
  AddressDto createOrganizationAddress(String orgId, AddressDto dto);
  void deleteOrganizationAddress(String orgId, String addressId);

}
