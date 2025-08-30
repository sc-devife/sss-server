package com.sss.app.entity.address;


import com.sss.app.AddressType;
import com.sss.app.dto.address.AddressDto;
import com.sss.app.dto.organizations.OrganizationsDto;
import com.sss.app.dto.users.UserUpdateRequestDto;
import com.sss.app.entity.organizations.Organizations;
import com.sss.app.util.CompareUtil;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "organization_address")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long seqp;

    @Column(insertable = false, updatable = false)
    private String uid;

    private String street;
    private String city;
    private String state;
    private String zipCode;
    private String country;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AddressType addressType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organizations organization;

    public static Address create(AddressDto dto, Organizations orgs) {

        AddressBuilder builder = Address.builder();
        builder.street(dto.getStreet());
        builder.city(dto.getCity());
        builder.state(dto.getState());
        builder.zipCode(dto.getZipCode());
        builder.country(dto.getCountry());
        builder.addressType(dto.getAddressType());
        builder.organization(orgs);
        builder.build();
        return builder.build();

    }


    public void update(AddressDto dto) {

        if (CompareUtil.hasChanged(dto.getStreet(), this.street)) {
            this.street = dto.getStreet();
        }

        if (CompareUtil.hasChanged(dto.getCity(), this.city)) {
            this.city = dto.getCity();
        }

        if (CompareUtil.hasChanged(dto.getState(), this.state)) {
            this.state = dto.getState();
        }
        if (CompareUtil.hasChanged(dto.getZipCode(), this.zipCode)) {
            this.zipCode = dto.getZipCode();
        }
        if (CompareUtil.hasChanged(dto.getCountry(), this.country)) {
            this.country = dto.getCountry();
        }
    }
}

