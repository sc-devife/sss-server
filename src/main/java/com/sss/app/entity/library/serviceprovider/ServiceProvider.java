package com.sss.app.entity.library.serviceprovider;

import com.sss.app.entity.common.Auditable;
import com.sss.app.entity.library.escapepoint.EscapePoint;
import com.sss.app.util.IdGenerator;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "service_providers")
@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceProvider extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long seqp;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID uid;

    private Long orgId;

    @Column(nullable = false)
    private String name;

    // single_select: transport/activity/guide/other (Section 15)
    @Column(name = "type_code", nullable = false)
    private String typeCode;

    // Kept for bulk-import compatibility (see ServiceProviderImportSchema) —
    // no longer shown on the Add/Edit form, which uses the three fields
    // below instead (same "legacy column untouched" precedent as Hotel's
    // own contactInfo -> phoneNumber/email split).
    @Column(name = "contact_info")
    private String contactInfo;

    @Column(name = "contact_name")
    private String contactName;

    @Column(name = "contact_number", length = 30)
    private String contactNumber;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "country_code")
    private String countryCode;

    // Meaning depends on typeCode — vehicles/activities/guides/"other" count,
    // labeled dynamically on the form (see ServiceProvidersPanel.tsx). One
    // column rather than four type-specific ones since it's always exactly
    // one quantity value regardless of which type is selected.
    @Column
    private Integer quantity;

    // Only meaningful when typeCode == "other" — the free-text description
    // of what "Other" actually means for this provider.
    @Column(name = "other_type_label")
    private String otherTypeLabel;

    // Which destination this provider serves — same "destination_id"
    // ManyToOne already used by Hotel/Transport/Activity.
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_id")
    private EscapePoint escapePoint;

    @Column
    private String status;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        if (this.uid == null) {
            this.uid = IdGenerator.newUid();
        }
    }
}
