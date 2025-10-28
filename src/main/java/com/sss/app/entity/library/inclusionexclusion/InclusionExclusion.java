package com.sss.app.entity.library.inclusionexclusion;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Table(name = "inclusion_exclusion_items")
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
public class InclusionExclusion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long seqp;

    @Column
    private Long sort_order;

    @Column
    private Long company_id;

    @Column(insertable = false, updatable = false)
    private String uid;

    @Column
    private String name;

    @Column
    private String type;

    @Column
    private String description;

    @Column
    private Boolean is_active;
}
