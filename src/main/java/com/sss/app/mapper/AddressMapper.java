package com.sss.app.mapper;

import com.sss.app.dto.address.AddressDto;
import com.sss.app.entity.address.Address;
import org.springframework.stereotype.Component;

@Component
public class AddressMapper {
    public AddressDto mapToDTO(Address address) {
        return AddressDto.builder()
                .id(address.getSeqp())
                .label(address.getLabel())
                .city(address.getCity())
                .state(address.getState())
                .country(address.getCountry())
                .zipCode(address.getZipCode())
                .streetFirst(address.getStreetFirst())
                .streetSecond(address.getStreetSecond())
                .landMark(address.getLandMark())
                .additionalDetails(address.getAdditionalDetails())
                .contactEmail(address.getContactEmail())
                .tripDestination(address.getTripDestination())
                //.addressType(address.getAddressType())
              //  .organizationId(address.getOrganization().getSeqp())
                .build();
    }
}
