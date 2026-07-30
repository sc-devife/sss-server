package com.sss.app.service;

import com.sss.app.dto.address.AddressDto;

import java.util.List;

public interface AddressService {
  List<AddressDto> getAddressesForOrg(Long orgId);
  AddressDto updateOrganizationAddress(Long orgId, Long addressId, AddressDto updateRequest);
  AddressDto createOrganizationAddress(Long orgId, AddressDto dto);
  void deleteOrganizationAddress(Long orgId, Long addressId);

}
