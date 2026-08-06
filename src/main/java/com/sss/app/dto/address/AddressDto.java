package com.sss.app.dto.address;
import com.sss.app.AddressType;
import jakarta.validation.constraints.Pattern;
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

    // Optional field — empty is valid, but if provided must be a syntactically
    // valid E.164 number (the frontend's PhoneInput always submits this shape;
    // real per-country length/format rules are enforced there via
    // libphonenumber-js, this is a defense-in-depth shape guard only).
    @Pattern(regexp = "^$|^\\+[1-9]\\d{6,14}$", message = "Enter a valid phone number")
    private String contactNumber;
    private String contactEmail;
    private String tripDestination;

    // allows multiple roles (Billing, Contact, etc.)
    private List<AddressType> addressTypes;

    private Boolean primaryAddress;

}
