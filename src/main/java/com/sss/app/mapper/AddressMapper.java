package com.sss.app.mapper;

import com.sss.app.dto.address.AddressDto;
import com.sss.app.entity.address.Address;
import org.springframework.stereotype.Component;

@Component
public class AddressMapper {
    public AddressDto mapToDTO(Address address) {
        return AddressDto.builder()
                .uid(address.getUid())
                .label(address.getLabel())
                .city(address.getCity())
                .state(address.getState())
                .country(address.getCountry())
                .zipCode(address.getZipCode())
                .streetFirst(address.getStreetFirst())
                .streetSecond(address.getStreetSecond())
                .landMark(address.getLandMark())
                .additionalDetails(address.getAdditionalDetails())
                .contactNumber(address.getContactNumber())
                .contactEmail(address.getContactEmail())
                .tripDestination(address.getTripDestination())
                .gstin(address.getGstin())
                .addressTypes(address.getConstraints().stream()
                        .map(com.sss.app.entity.address.AddressConstraint::getAddressType)
                        .distinct()
                        .toList())
                .primaryAddress(address.getConstraints().stream()
                        .anyMatch(com.sss.app.entity.address.AddressConstraint::isPrimaryAddress))
                .build();
    }
}
