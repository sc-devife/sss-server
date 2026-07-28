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
import java.util.UUID;


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

    @Column (name = "support_ph_num")
    private String supportPhNum;

    private String countryCode;

    private String defaultCurrencyCode;

    private String logoFile;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OrganizationStatus status = OrganizationStatus.ACTIVE;

    // Templates don't exist as their own tables yet (Phase 5/6) — these are
    // forward-looking columns only, no FK until that table is built.
    private UUID quoteTemplateId;
    private UUID invoiceTemplateId;

    // Section 5: "auto-assignment runs when a lead is created if the org
    // has it enabled; otherwise leads land in an unassigned queue."
    @Builder.Default
    private Boolean autoAssignEnabled = true;

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
        builder.defaultCurrencyCode(dto.getDefault_currency_code());
        builder.logoFile(dto.getLogo_file());

        return builder.build();
    }

    public void update(OrganizationsDto dto) {

        if (dto.getRegistered_name() != null && CompareUtil.hasChanged(dto.getRegistered_name(), this.registeredName)) {
            this.registeredName = dto.getRegistered_name();
        }

        if (CompareUtil.hasChanged(dto.getDisplay_name(), this.displayName)) {
            this.displayName = dto.getDisplay_name();
        }
        if (CompareUtil.hasChanged(dto.getSupport_ph_num(), this.getSupportPhNum())) {
            this.supportPhNum = dto.getSupport_ph_num();
        }
        if (CompareUtil.hasChanged(dto.getCountry_code(), this.countryCode)) {
            this.countryCode = dto.getCountry_code();
        }
        if (CompareUtil.hasChanged(dto.getDefault_currency_code(), this.defaultCurrencyCode)) {
            this.defaultCurrencyCode = dto.getDefault_currency_code();
        }
        if (CompareUtil.hasChanged(dto.getLogo_file(), this.logoFile)) {
            this.logoFile = dto.getLogo_file();
        }
        // quote_template_id was on this DTO from the start (Section 15) but
        // never actually applied here — dead until Phase 5's template
        // registry gave it something real to reference.
        if (dto.getQuote_template_id() != null && CompareUtil.hasChanged(dto.getQuote_template_id(), this.quoteTemplateId)) {
            this.quoteTemplateId = dto.getQuote_template_id();
        }
        if (dto.getInvoice_template_id() != null && CompareUtil.hasChanged(dto.getInvoice_template_id(), this.invoiceTemplateId)) {
            this.invoiceTemplateId = dto.getInvoice_template_id();
        }
        if (dto.getAuto_assign_enabled() != null && CompareUtil.hasChanged(dto.getAuto_assign_enabled(), this.autoAssignEnabled)) {
            this.autoAssignEnabled = dto.getAuto_assign_enabled();
        }
    }
}
