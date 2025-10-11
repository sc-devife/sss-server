package com.sss.app.entity.escapepoint;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Table(name = "escape_points")
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
public class EscapePoint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long seqp;

    @Column
    private Long company_id;

    @Column(insertable = false, updatable = false)
    private String uid;

    @Column
    private String id;

    @Column
    private String name;

    @Column
    private String nick_name;

    @Column
    private String city;

    @Column
    private String province;

    @Column
    private String country;

    @Column
    private String region;

    @Column
    private String nearest_airport;

    @Column
    private String currency;

    @Column
    private String time_zone;

    @Column
    private String tags;

    @Column
    private String remarks;
}
