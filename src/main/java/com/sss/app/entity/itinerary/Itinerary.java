package com.sss.app.entity.itinerary;

import com.sss.app.entity.common.Auditable;
import com.sss.app.entity.escape.Escape;
import com.sss.app.util.IdGenerator;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.UUID;

@Entity
@Table(name = "itineraries")
@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Itinerary extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seqp;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID uid;

    private Long orgId;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "escape_id", nullable = false)
    private Escape escape;

    @Column(nullable = false)
    private String name;

    // draft / active / superseded
    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private Integer version;

    @PrePersist
    protected void onCreate() {
        if (this.uid == null) {
            this.uid = IdGenerator.newUid();
        }
        if (this.status == null) {
            this.status = "draft";
        }
        if (this.version == null) {
            this.version = 1;
        }
    }
}
