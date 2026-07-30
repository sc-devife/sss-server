package com.sss.app.entity.users.invitations;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

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
    private Long orgId;

    @Column
    private Long invitedBy;

    @Column
    private String email;

    @Column
    private LocalDateTime expires_set;

    @Column
    private boolean is_used;

    @Column
    private boolean is_archived;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "roles", columnDefinition = "text[]")
    private List<String> roles;
}
