package com.sss.app.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;


@Entity
@Table(name = "organizations")
@Data
public class Organizations {
    @Id
    @Column
    private Long seqp;

    @Column (name = "registered_name")
    private String registeredName;

    @Column (name = "display_name")
    private String displayName;

    @Column (name = "support_ph_num")
    private String supportPhNum;
}
