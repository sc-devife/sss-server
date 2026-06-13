package com.sss.app.entity.library.escapesource;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "escape_source_b2b_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EscapeSourceB2BDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seqp;

    @OneToOne
    @JoinColumn(name = "seqa")
    private EscapeSource escapeSource;

    private String contactName;
    private String contactEmail;
    private String contactPhone;

    private String city;
    private String state;
    private String country;
    private String pincode;

    private String streetAddress;
    private String locality;
    private String landmark;

    private String billingName;

    @Column(columnDefinition = "TEXT")
    private String additionalBillingDetails;
}
