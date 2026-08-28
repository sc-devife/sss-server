package com.sss.app.entity.library.escapepoint;

import com.sss.app.entity.common.Auditable;
import com.sss.app.entity.library.hotel.Hotel;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.util.List;

@Entity
@Table(name = "escape_points")
@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
public class EscapePoint extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long seqp;

    @Column
    private Long orgId;

    @Column(insertable = false, updatable = false)
    private String uid;

    @Column
    private String id;

    @Column
    private String name;

    @Column
    private String nick_name;

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

    @Column
    private String description;

    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.ARRAY)
    @Column(columnDefinition = "text[]")
    private List<String> images;

    @Column
    private String status;

    @Column(name = "deleted_at")
    private java.time.LocalDateTime deletedAt;

    @ManyToMany(mappedBy = "escapePoints")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Hotel> hotels;
}
