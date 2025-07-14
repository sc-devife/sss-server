package com.sss.app.entity.organizations;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "organizations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Organizations {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "org_seq_gen")
    @SequenceGenerator(
            name = "org_seq_gen",               // internal name for JPA
            sequenceName = "organizations_seqp_seq", // actual Postgres sequence name
            allocationSize = 1                 // match DB sequence increment (usually 1)
    )
    @Column(name = "seqp")
    private Long seqp;

    @Column (name = "registered_name")
    private String registeredName;

    @Column (name = "display_name")
    private String displayName;

    @Column (name = "support_ph_num")
    private String supportPhNum;
}
