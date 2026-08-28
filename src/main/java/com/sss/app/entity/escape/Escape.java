package com.sss.app.entity.escape;

import com.sss.app.entity.common.Auditable;
import com.sss.app.entity.lead.Lead;
import com.sss.app.entity.library.escapepoint.EscapePoint;
import com.sss.app.entity.traveller.Traveller;
import com.sss.app.util.IdGenerator;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "escapes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Escape extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seqp;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID uid;

    private Long orgId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lead_id", nullable = false) //rename
    private Lead lead;

    // Assignment lives here, not on Lead — decided once, by the assignment
    // engine, at the moment a lead is converted (see EscapeHelper.createEscape
    // and LeadAssignmentService). Never inherited from Lead.assignedToUserId,
    // which no longer exists — leads are never individually assigned.
    @Column(name = "assigned_to_user_id")
    private Long assignedToUserId;

    @Column(name = "assignment_reason")
    private String assignmentReason;

    @ManyToMany
    @JoinTable(
            name = "escape_traveller",
            joinColumns = @JoinColumn(name = "escape_id"),
            inverseJoinColumns = @JoinColumn(name = "traveller_id")
    )
   // private List<Traveller> travellers;
    private Set<Traveller> travellers = new HashSet<>();;

    @ManyToMany
    @JoinTable(
            name = "escape_destination",
            joinColumns = @JoinColumn(name = "escape_id"),
            inverseJoinColumns = @JoinColumn(name = "escape_point_id")
    )
    private Set<EscapePoint> escapePoints = new HashSet<>();

    // The traveller who represents the lead's original/primary contact, set
    // once at creation time (see EscapeHelper.createEscape) rather than
    // inferred from travellers' set order, which has no guaranteed ordering.
    // Nullable: escapes created before this field existed have no primary.
    @Column(name = "primary_traveller_uid")
    private UUID primaryTravellerUid;

    private String status;

    private LocalDate startDate;
    private Integer numberOfDays;
    // ✅ AUTO CALCULATED
    private LocalDate endDate;

  /*  public void setTravellers(List<Escape> allById) {
    }*/

    @PrePersist
    protected void onCreate() {
        if (this.uid == null) {
            this.uid = IdGenerator.newUid();
        }
    }
}
