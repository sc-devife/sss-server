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



    @OneToMany(mappedBy = "address", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AddressConstraint> constraints = new ArrayList<>();


   public static Address create(AddressDto dto) {

        AddressBuilder builder = Address.builder();
        builder.label(dto.getLabel());
        builder.city(dto.getCity());
        builder.state(dto.getState());
        builder.country(dto.getCountry());
        builder.zipCode(dto.getZipCode());
        builder.streetFirst(dto.getStreetFirst());
        builder.streetSecond(dto.getStreetSecond());
        builder.landMark(dto.getLandMark());
        builder.additionalDetails(dto.getAdditionalDetails());
        builder.contactEmail(dto.getContactEmail());
        builder.tripDestination(dto.getTripDestination());
        builder.build();
        return builder.build();

    }

    public void update(AddressDto dto) {

        if (dto.getLabel() != null && CompareUtil.hasChanged(dto.getLabel(), this.label)) {
            this.label = dto.getLabel();
        }
        if (dto.getCity() != null && CompareUtil.hasChanged(dto.getCity(), this.city)) {
            this.city = dto.getCity();
        }
        if (dto.getState() != null && CompareUtil.hasChanged(dto.getState(), this.state)) {
            this.state = dto.getState();
        }
        if (dto.getCountry() != null && CompareUtil.hasChanged(dto.getCountry(), this.country)) {
            this.country = dto.getCountry();
        }
        if (dto.getZipCode() != null && CompareUtil.hasChanged(dto.getZipCode(), this.zipCode)) {
            this.zipCode = dto.getZipCode();
        }
        if (dto.getStreetFirst() != null && CompareUtil.hasChanged(dto.getStreetFirst(), this.streetFirst)) {
            this.streetFirst = dto.getStreetFirst();
        }
        if (dto.getStreetSecond() != null && CompareUtil.hasChanged(dto.getStreetSecond(), this.streetSecond)) {
            this.streetSecond = dto.getStreetSecond();
        }

        if (dto.getLandMark() != null && CompareUtil.hasChanged(dto.getLandMark(), this.landMark)) {
            this.landMark = dto.getLandMark();
        }

        if (dto.getAdditionalDetails() != null && CompareUtil.hasChanged(dto.getAdditionalDetails(), this.additionalDetails)) {
            this.additionalDetails = dto.getAdditionalDetails();
        }
        if (dto.getContactEmail() != null && CompareUtil.hasChanged(dto.getContactEmail(), this.contactEmail)) {
            this.contactEmail = dto.getContactEmail();
        }
        if (dto.getTripDestination() != null && CompareUtil.hasChanged(dto.getTripDestination(), this.tripDestination)) {
            this.tripDestination = dto.getTripDestination();
        }
    }
}

