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

    // Computed by the DB from seqp (see V82 migration) — human-readable trip
    // code for quotation headers/watermarks, e.g. "TRP-000123". Never
    // app-generated so it can never drift out of sync with seqp.
    @Column(name = "trip_code", insertable = false, updatable = false)
    private String tripCode;

    private Long orgId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lead_id", nullable = false) //rename
    private Lead lead;

    // Set by the assignment engine at conversion time (see
    // EscapeHelper.createEscape / LeadAssignmentServiceImpl.autoAssign). If
    // the source Lead already carries its own assignment (Lead.
    // assignedToUserId, set by autoAssignLead at intake), that's carried
    // over as-is instead of re-running the engine, so the same agent keeps
    // the deal from lead through conversion; otherwise the engine picks
    // fresh here, same as before Lead-level routing existed.
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

    // Idempotency stamp for the "travel date approaching" notification sweep
    // (EscapeTravelDateReminderServiceImpl) — same "stamp once sent, never
    // cleared" role FollowUp's own reminder columns play.
    @Column(name = "travel_date_reminder_sent_at")
    private java.time.LocalDateTime travelDateReminderSentAt;

    // Private, team-only notes — never exposed to QuotationDataService, so
    // it can never reach the client-facing quotation.
    @Column(name = "internal_comments", columnDefinition = "TEXT")
    private String internalComments;

    // Client-facing rich text, sanitized the same way as Terms/Inclusions/
    // Exclusions content (see RichTextSanitizer) — flows into the Quotation
    // via QuotationDataService.
    @Column(name = "remark_for_lead", columnDefinition = "TEXT")
    private String remarkForLead;

    // Set/updated only via EscapeLifecycleService.hold() — the date the
    // escape's Hold status is tied to (see EscapeStatus.HOLD).
    @Column(name = "hold_date")
    private LocalDate holdDate;

  /*  public void setTravellers(List<Escape> allById) {
    }*/

    @PrePersist
    protected void onCreate() {
        if (this.uid == null) {
            this.uid = IdGenerator.newUid();
        }
    }
}
