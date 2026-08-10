package com.sss.app.entity.library.escapesource;

import com.sss.app.util.IdGenerator;
import com.sss.app.util.escapeSource.EscapeSourceType;
import com.sss.app.util.escapeSource.EscapeSourceStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "escape_sources")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EscapeSource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seqp;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID uid;

    @Column(name = "org_id")
    private Long orgId;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false)
    private EscapeSourceType sourceType;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "short_name")
    private String shortName;

    @Column(name = "trip_source_tag")
    private String escapeSourceTag;

    @Enumerated(EnumType.STRING)
    private EscapeSourceStatus status = EscapeSourceStatus.ACTIVE;

    @OneToOne(mappedBy = "escapeSource", cascade = CascadeType.ALL)
    private EscapeSourceB2BDetails b2bDetails;

    @OneToOne(mappedBy = "escapeSource", cascade = CascadeType.ALL)
    private EscapeSourceDirectDetails directDetails;

    @PrePersist
    protected void onCreate() {
        if (this.uid == null) {
            this.uid = IdGenerator.newUid();
        }
    }
}
