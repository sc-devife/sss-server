package com.sss.app.mapper;

import com.sss.app.dto.address.AddressDto;
import com.sss.app.entity.address.Address;
import org.springframework.stereotype.Component;

@Component
public class AddressMapper {
    public AddressDto mapToDTO(Address address) {
        return AddressDto.builder()
                .id(address.getSeqp())
                .street(address.getStreet())
                .city(address.getCity())
                .state(address.getState())
                .zipCode(address.getZipCode())
                .country(address.getCountry())
                //.addressType(address.getAddressType())
              //  .organizationId(address.getOrganization().getSeqp())
                .build();
    }
}
