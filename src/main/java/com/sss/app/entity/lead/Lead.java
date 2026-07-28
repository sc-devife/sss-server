package com.sss.app.entity.lead;

import com.sss.app.entity.common.Auditable;
import com.sss.app.entity.library.escapepoint.EscapePoint;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;

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
    private Long orgId;
    private String name;
    private String email;
    private String phone;
    private String destination;     // e.g. "Bali" — free text, kept for historical rows (see destination below)
    private Integer numberOfPeople; // e.g. 4
    private LocalDate travelDate;   // e.g. 2025-12-15
    private Integer durationDays;   // e.g. 7 days
    private Double budget;          // e.g. 150000.00
    private String status;          // system-managed lifecycle: New/Contacted/Qualified/Converted/Unqualified/Lost/Duplicate

    // Section 7: source_code (manual/whatsapp/instagram/youtube/google_ads/agency)
    // + the external channel's own id for this lead, for dedup/ack.
    @Column(name = "source_code")
    private String sourceCode;

    @Column(name = "source_ref_id")
    private String sourceRefId;

    // Section 15 destination_interest — coded FK alongside the historical
    // free-text `destination` column above (see V14 migration note).
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_id")
    private EscapePoint destinationRef;

    @Column(name = "assigned_to_user_id")
    private Long assignedToUserId;

    @Column(name = "assignment_reason")
    private String assignmentReason;

    @Column(name = "is_priority")
    private Boolean isPriority;

    // Section 5's fuller priority-detection rules (metro-city origin,
    // honeymoon/family + vacation season) — see LeadAssignmentServiceImpl.
    @Column(name = "origin_city")
    private String originCity;

    @Column(name = "travel_type")
    private String travelType; // honeymoon/family/friends/solo/business/other

    @Column(length = 500)
    private String notes;
}
