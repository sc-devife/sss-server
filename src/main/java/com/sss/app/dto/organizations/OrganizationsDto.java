package com.sss.app.dto.organizations;

import com.sss.app.entity.organizations.OrganizationStatus;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

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
    private String default_currency_code;
    private String logo_file;
    private OrganizationStatus status;
    private UUID quote_template_id;
    private UUID invoice_template_id;
    private Boolean auto_assign_enabled;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;
}
