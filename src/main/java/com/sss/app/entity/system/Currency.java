package com.sss.app.entity.system;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Table(name = "supported_currencies")
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
@Builder
public class Currency {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long seqp;

    @Column(insertable = false, updatable = false)
    private String uid;

    @Column
    private String code;

    @Column
    private String name;

    @Column
    private String symbol;

    @Column
    private Boolean is_active;

    // ISO 4217 minor-unit digits (2 for INR/AED, 0 for JPY, 3 for KWD).
    @Column
    private Integer minor_units;
}
