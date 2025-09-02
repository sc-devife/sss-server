package com.sss.app.dto.address;
import com.sss.app.AddressType;
import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressDto {

    private Long id;                 // optional in create, useful in response
    private String street;
    private String city;
    private String state;
    private String zipCode;
    private String country;

    // allows multiple roles (Billing, Contact, etc.)
    private List<AddressType> addressTypes;
}
