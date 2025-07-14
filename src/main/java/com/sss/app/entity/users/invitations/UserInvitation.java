package com.sss.app.entity.users.invitations;

import jakarta.persistence.*;

import java.time.LocalDateTime;

import lombok.Data;

@Entity
@Table(name = "user_invitations")
@Data
public class UserInvitation {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seqp;

    @Column(insertable = false, updatable = false)
    private String uid;

    @Column
    private String email;

    @Column
    private LocalDateTime expires_set;

    @Column
    private boolean is_used;

    @Column
    private boolean is_archived;
}
