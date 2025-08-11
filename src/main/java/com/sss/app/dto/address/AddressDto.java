package com.sss.app.dto.address;
import com.sss.app.AddressType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressDto {

    private Long id;
    private String street;
    private String city;
    private String state;
    private String zipCode;
    private String country;
    private AddressType addressType;
    private Long organizationId;
}
