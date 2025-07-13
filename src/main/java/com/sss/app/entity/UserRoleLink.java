package com.sss.app.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "user_role_links")
@Data
public class UserRoleLink {

    @Id
    @Column
    private Long seqp;

    @Column
    private String uid;

    @ManyToOne
    @JoinColumn(name = "seqa")
    @JsonBackReference
    private User user;

    @ManyToOne
    @JoinColumn(name = "seqb")
    private Role role;

    @Column
    private Boolean is_active;

    @Column
    private Boolean is_archived;
}