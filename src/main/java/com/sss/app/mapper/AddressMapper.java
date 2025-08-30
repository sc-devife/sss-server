package com.sss.app.mapper;

import com.sss.app.dto.address.AddressDto;
import com.sss.app.dto.userrolelinks.UserRoleLinkResponseDto;
import com.sss.app.dto.users.UserResponseDto;
import com.sss.app.entity.address.Address;
import com.sss.app.entity.userrolelinks.UserRoleLink;
import com.sss.app.entity.users.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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
                .addressType(address.getAddressType())
                .organizationId(address.getOrganization().getSeqp())
                .build();
    }
  /*  public AddressDto toAddressResponseDto(Address address) {
        AddressDto dto = new AddressDto();
       // dto.set(address.getSeqp());
       // dto.setUid(address.getUid());
        dto.setStreet(address.getStreet());
        dto.setCity(address.getCity());
        dto.setState(address.getState());
        dto.setZipCode(address.getZipCode());
        dto.setCountry(address.getCountry());
        dto.setAddressType(address.getAddressType());
        dto.setContact_number(address.getAddressType());

        List<UserRoleLinkResponseDto> roleDTOs = new ArrayList<>();
        for (UserRoleLink link : user.getRoles()) {
            UserRoleLinkResponseDto urDto = getUserRoleLinkDto(link);
            roleDTOs.add(urDto);
        }

        dto.setRoles(roleDTOs);
        return dto;
    }*/
}
