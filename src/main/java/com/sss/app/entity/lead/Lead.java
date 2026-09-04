package com.sss.app.entity.lead;

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

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "leads")
@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lead extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seqp;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID uid;

    private Long orgId;
    private String name;
    private String email;
    private String phone;
    private String destination;     // e.g. "Bali" — free text, kept for historical rows (see destination below)
    private Integer numberOfPeople; // e.g. 4
    private LocalDate travelDate;   // e.g. 2025-12-15
    private Integer durationNights; // e.g. 3 nights (implies a 4-day trip — see LeadAssignmentServiceImpl/ConvertToEscapeModal for the "+1" used when this becomes Escape.numberOfDays)
    private Double budget;          // e.g. 150000.00
    private String status;          // system-managed lifecycle: New/Contacted/Qualified/Converted/Unqualified/Lost/Duplicate

    // DIRECT | AGENCY — who the lead's business relationship is with. DIRECT
    // leads carry a sourceChannel (which inbound channel produced them);
    // AGENCY leads carry a LeadAgencyDetails record instead (see below).
    @Column(name = "source_type")
    private String sourceType;

    // Only meaningful when sourceType = DIRECT: manual/whatsapp/instagram/
    // youtube/google_ads. Null for AGENCY leads.
    @Column(name = "source_channel")
    private String sourceChannel;

    @Column(name = "source_ref_id")
    private String sourceRefId;

    // Section 15 destination_interest — coded links alongside the historical
    // free-text `destination` column above (see V14 migration note). Was a
    // single @ManyToOne (destination_id) until V80, which moved it to this
    // many-to-many so a lead can express interest in more than one Escape
    // Point — mirrors Escape.escapePoints / Hotel.escapePoints.
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "lead_escape_points",
            joinColumns = @JoinColumn(name = "lead_id"),
            inverseJoinColumns = @JoinColumn(name = "escape_point_id")
    )
    @Builder.Default
    private Set<EscapePoint> escapePoints = new HashSet<>();

    @Builder.Default
    @Column(name = "is_priority", nullable = false)
    private Boolean isPriority = false;

    // Section 5's fuller priority-detection rules (metro-city origin,
    // honeymoon/family + vacation season) — see LeadAssignmentServiceImpl.
    @Column(name = "origin_city")
    private String originCity;

    @Column(name = "travel_type")
    private String travelType; // honeymoon/family/friends/solo/business/other

    @Column(length = 500)
    private String notes;

    @Column(name = "follow_up_due_date")
    private LocalDate followUpDueDate;

    @PrePersist
    protected void onCreate() {
        if (this.uid == null) {
            this.uid = IdGenerator.newUid();
        }
    }
}
