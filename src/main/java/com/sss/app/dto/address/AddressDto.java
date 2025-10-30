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
    private String label;
    private String city;
    private String state;
    private String country;
    private String zipCode;
    private String streetFirst;
    private String streetSecond;
    private String landMark;
    private String additionalDetails;
    //private String contactNumber;
    private String contactEmail;
    private String tripDestination;

    // allows multiple roles (Billing, Contact, etc.)
    private List<AddressType> addressTypes;

    private Boolean primaryAddress;

}
