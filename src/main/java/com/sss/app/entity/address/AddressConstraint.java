package com.sss.app.entity.address;

import com.sss.app.AddressType;
import com.sss.app.dto.address.AddressDto;
import com.sss.app.entity.organizations.Organizations;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "address_constraints")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressConstraint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seqp;

    @Enumerated(EnumType.STRING)
    @Column(name = "address_type", nullable = false)
    private AddressType addressType;

    @ManyToOne
    @JoinColumn(name = "seqb", nullable = false)   // FK to Organizations
    private Organizations organization;

    @Column(name = "seqb_type", nullable = false) // Just a plain column
    private String seqbType;

    @ManyToOne
    @JoinColumn(name = "seqa", nullable = false)   // FK to Address
    private Address address;

    @Column(name = "seqa_type", nullable = false) // Just a plain column
    private String seqaType;

    @Column(name = "is_primary", nullable = false)
    private boolean primaryAddress;

    public static AddressConstraint create(Organizations org, Address savedAddress, AddressType type, boolean isPrimary) {

        AddressConstraint constraint = AddressConstraint.builder()
                .organization(org)
                .address(savedAddress)
                .addressType(type)
                .seqbType("Organization")   // ✅ must set
                .seqaType("Address")
                .primaryAddress(isPrimary)
                .build();

        return constraint;
    }
}
