package com.sss.app.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @Column
    private Long seqp;

    @Column
    private String uid;

    @Column
    private String name;

    @Column
    private String email;

    @Column
    private String first_name;

    @Column
    private String last_name;

    @Column
    private String contact_number;
}
