package com.sss.app.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "roles")
@Data
public class Role {

    @Id
    @Column
    private Long seqp;

    @Column
    private String uid;

    @Column
    private String name;

    @Column
    private String label;

    @Column
    private Boolean is_active;

    @Column
    private Boolean is_archived;
}