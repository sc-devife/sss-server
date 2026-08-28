package com.sss.app.dto.organizations;

import com.sss.app.entity.organizations.OrganizationStatus;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrganizationsDto {
    private Long seqp;
    private String uid;
    private String registered_name;
    private String display_name;
    private String org_code;

    // Optional field — empty is valid, but if provided must be a syntactically
    // valid E.164 number (the frontend's PhoneInput always submits this shape;
    // real per-country length/format rules are enforced there via
    // libphonenumber-js, this is a defense-in-depth shape guard only).
    @Pattern(regexp = "^$|^\\+[1-9]\\d{6,14}$", message = "Enter a valid phone number")
    private String support_ph_num;
    private String country_code;
    private String logo_file;
    private String logo_shape;
    private OrganizationStatus status;

    private String pan;
    private String legal_entity_type;
    private String cin;
    private String business_email;
    private String website_url;
    private String whatsapp_number;
    private String tagline;
    private String about_text;
    private String industry_accreditation;

    private LocalDateTime created_at;
    private LocalDateTime updated_at;

    // Nested rather than a flat merge — keeps the profile/settings split
    // visible on the wire too, not just in the DB. Populated on read;
    // updateOrganizations() applies it via a separate settings update path.
    private OrganizationSettingsDto settings;
}
