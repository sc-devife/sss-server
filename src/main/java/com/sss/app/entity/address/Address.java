package com.sss.app.entity.address;


import com.sss.app.AddressType;
import com.sss.app.dto.address.AddressDto;
import com.sss.app.dto.organizations.OrganizationsDto;
import com.sss.app.dto.users.UserUpdateRequestDto;
import com.sss.app.entity.organizations.Organizations;
import com.sss.app.util.CompareUtil;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

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

    @OneToMany(mappedBy = "address", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AddressConstraint> constraints = new ArrayList<>();


   public static Address create(AddressDto dto) {

        AddressBuilder builder = Address.builder();
        builder.street(dto.getStreet());
        builder.city(dto.getCity());
        builder.state(dto.getState());
        builder.zipCode(dto.getZipCode());
        builder.country(dto.getCountry());
        builder.build();
        return builder.build();

    }

    public void update(AddressDto dto) {

        if (dto.getStreet() != null && CompareUtil.hasChanged(dto.getStreet(), this.street)) {
            System.out.println("dto get street == " + dto.getStreet());
            this.street = dto.getStreet();
        }

        if (dto.getCity() != null && CompareUtil.hasChanged(dto.getCity(), this.city)) {
            System.out.println("dto get city == " + dto.getCity());
            this.city = dto.getCity();
        }

        if (dto.getState() != null && CompareUtil.hasChanged(dto.getState(), this.state)) {
            System.out.println("dto get getState == " + dto.getState());
            this.state = dto.getState();
        }
        if (dto.getZipCode() != null && CompareUtil.hasChanged(dto.getZipCode(), this.zipCode)) {
            System.out.println("dto get getZipCode == " + dto.getZipCode());
            this.zipCode = dto.getZipCode();
        }
        if (dto.getCountry() != null && CompareUtil.hasChanged(dto.getCountry(), this.country)) {
            System.out.println("dto get getCountry == " + dto.getCountry());
            this.country = dto.getCountry();
        }

    }
}

