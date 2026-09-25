package com.sss.app.entity.library.activity;

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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "activities")
@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Activity extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long seqp;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID uid;

    private Long orgId;

    @Column(nullable = false)
    private String name;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_id")
    private EscapePoint escapePoint;

    // single_select: water sports/sightseeing/adventure (Section 15)
    @Column(name = "category_code")
    private String categoryCode;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column
    private String description;

    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.ARRAY)
    @Column(columnDefinition = "text[]")
    private List<String> images;

    @Column(name = "base_price", precision = 14, scale = 4)
    private BigDecimal basePrice;

    // Currency of this item's prices; null = the vendor's base currency (V136).
    @Column(name = "price_currency")
    private String priceCurrency;

    @Column
    private String status;

    @Column
    private String notes;

    // Vendor/supplier email — recipient for the "Send Booking Email"
    // booking-request flow (see HotelBookingEmailService's Hotel.email
    // counterpart).
    @Column
    private String email;

    @Column(name = "contact_number")
    private String contactNumber;

    // HTML from the RichTextEditor on the Add/Edit Activity form.
    @Column(name = "rules_and_policies", columnDefinition = "TEXT")
    private String rulesAndPolicies;

    // ----- Account tab: this activity vendor's own payout details (bank
    // account or UPI) — same shape/purpose as Hotel's own Account tab. -----
    @Column(name = "account_holder_name")
    private String accountHolderName;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "branch_name")
    private String branchName;

    // "Savings" or "Current" — plain string, not an enum.
    @Column(name = "account_type", length = 20)
    private String accountType;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(length = 20)
    private String ifsc;

    @Column(name = "upi_id")
    private String upiId;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        if (this.uid == null) {
            this.uid = IdGenerator.newUid();
        }
    }
}
