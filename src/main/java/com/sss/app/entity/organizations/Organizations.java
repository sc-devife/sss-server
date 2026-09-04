package com.sss.app.entity.organizations;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.sss.app.dto.organizations.OrganizationsDto;
import com.sss.app.entity.address.AddressConstraint;
import com.sss.app.util.CompareUtil;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "organizations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Organizations {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "org_seq_gen")
    @SequenceGenerator(
            name = "org_seq_gen",
            sequenceName = "organizations_seqp_seq",
            allocationSize = 1
    )
    @Column(name = "seqp")
    private Long seqp;

    @Column(insertable = false, updatable = false)
    private String uid;

    @Column (name = "registered_name")
    private String registeredName;

    @Column (name = "display_name")
    private String displayName;

    // Short, unique, human-readable id derived from display_name at
    // creation time — set once by OrganizationsHelper.createOrganizations,
    // never touched by update() (stable identifier, not re-slugged every
    // time the display name changes).
    @Column(name = "org_code")
    private String orgCode;

    @Column (name = "support_ph_num")
    private String supportPhNum;

    private String countryCode;

    private String logoFile;

    // "round" | "square" | "rectangle" — how the header/sidebar render
    // logoFile; free-form string rather than an enum since it's purely a
    // display hint, not a value anything branches on server-side.
    @Builder.Default
    private String logoShape = "round";

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OrganizationStatus status = OrganizationStatus.ACTIVE;

    // ----- Legal / compliance identity -----
    // One PAN per legal entity — moved off organization_address (V60), where
    // it was previously duplicated per-address. GSTIN correctly stays on
    // Address since GST registration is legitimately per-state in India.
    private String pan;
    private String legalEntityType;
    private String cin;

    // ----- Brand / presentation -----
    private String businessEmail;
    private String websiteUrl;
    private String instagramUrl;
    private String linkedinUrl;
    private String whatsappNumber;
    private String tagline;
    @Column(columnDefinition = "text")
    private String aboutText;
    private String industryAccreditation;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(updatable = false)
    private Long createdBy;

    @LastModifiedBy
    private Long updatedBy;

    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference // prevents infinite recursion
    @ToString.Exclude      // prevent Lombok from recursing in toString()
    @EqualsAndHashCode.Exclude
    private List<AddressConstraint> addressConstraints = new ArrayList<>();

    public static Organizations create(OrganizationsDto dto) {

        OrganizationsBuilder builder = Organizations.builder();
        builder.registeredName(dto.getRegistered_name());
        builder.displayName(dto.getDisplay_name());
        builder.supportPhNum(dto.getSupport_ph_num());
        builder.countryCode(dto.getCountry_code());
        builder.logoFile(dto.getLogo_file());
        if (dto.getLogo_shape() != null) {
            builder.logoShape(dto.getLogo_shape());
        }
        builder.pan(dto.getPan());
        builder.legalEntityType(dto.getLegal_entity_type());
        builder.cin(dto.getCin());
        builder.businessEmail(dto.getBusiness_email());
        builder.websiteUrl(dto.getWebsite_url());
        builder.instagramUrl(dto.getInstagram_url());
        builder.linkedinUrl(dto.getLinkedin_url());
        builder.whatsappNumber(dto.getWhatsapp_number());
        builder.tagline(dto.getTagline());
        builder.aboutText(dto.getAbout_text());
        builder.industryAccreditation(dto.getIndustry_accreditation());

        return builder.build();
    }

    public void update(OrganizationsDto dto) {

        if (dto.getRegistered_name() != null && CompareUtil.hasChanged(dto.getRegistered_name(), this.registeredName)) {
            this.registeredName = dto.getRegistered_name();
        }

        if (dto.getDisplay_name() != null && CompareUtil.hasChanged(dto.getDisplay_name(), this.displayName)) {
            this.displayName = dto.getDisplay_name();
        }
        if (dto.getSupport_ph_num() != null && CompareUtil.hasChanged(dto.getSupport_ph_num(), this.getSupportPhNum())) {
            this.supportPhNum = dto.getSupport_ph_num();
        }
        if (dto.getCountry_code() != null && CompareUtil.hasChanged(dto.getCountry_code(), this.countryCode)) {
            this.countryCode = dto.getCountry_code();
        }
        if (dto.getLogo_file() != null && CompareUtil.hasChanged(dto.getLogo_file(), this.logoFile)) {
            this.logoFile = dto.getLogo_file();
        }
        if (dto.getLogo_shape() != null && CompareUtil.hasChanged(dto.getLogo_shape(), this.logoShape)) {
            this.logoShape = dto.getLogo_shape();
        }
        if (dto.getPan() != null && CompareUtil.hasChanged(dto.getPan(), this.pan)) {
            this.pan = dto.getPan();
        }
        if (dto.getLegal_entity_type() != null && CompareUtil.hasChanged(dto.getLegal_entity_type(), this.legalEntityType)) {
            this.legalEntityType = dto.getLegal_entity_type();
        }
        if (dto.getCin() != null && CompareUtil.hasChanged(dto.getCin(), this.cin)) {
            this.cin = dto.getCin();
        }
        if (dto.getBusiness_email() != null && CompareUtil.hasChanged(dto.getBusiness_email(), this.businessEmail)) {
            this.businessEmail = dto.getBusiness_email();
        }
        if (dto.getWebsite_url() != null && CompareUtil.hasChanged(dto.getWebsite_url(), this.websiteUrl)) {
            this.websiteUrl = dto.getWebsite_url();
        }
        if (dto.getInstagram_url() != null && CompareUtil.hasChanged(dto.getInstagram_url(), this.instagramUrl)) {
            this.instagramUrl = dto.getInstagram_url();
        }
        if (dto.getLinkedin_url() != null && CompareUtil.hasChanged(dto.getLinkedin_url(), this.linkedinUrl)) {
            this.linkedinUrl = dto.getLinkedin_url();
        }
        if (dto.getWhatsapp_number() != null && CompareUtil.hasChanged(dto.getWhatsapp_number(), this.whatsappNumber)) {
            this.whatsappNumber = dto.getWhatsapp_number();
        }
        if (dto.getTagline() != null && CompareUtil.hasChanged(dto.getTagline(), this.tagline)) {
            this.tagline = dto.getTagline();
        }
        if (dto.getAbout_text() != null && CompareUtil.hasChanged(dto.getAbout_text(), this.aboutText)) {
            this.aboutText = dto.getAbout_text();
        }
        if (dto.getIndustry_accreditation() != null && CompareUtil.hasChanged(dto.getIndustry_accreditation(), this.industryAccreditation)) {
            this.industryAccreditation = dto.getIndustry_accreditation();
        }
    }
}
