package com.sss.app.entity.userrolelinks;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.sss.app.entity.roles.Role;
import com.sss.app.entity.users.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Table(name = "user_role_links")
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
@Builder
public class UserRoleLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long seqp;

    @Column(insertable = false, updatable = false)
    private Long seqa;

    @Column
    private String seqa_type;

    @Column(insertable = false, updatable = false)
    private Long seqb;

    @Column
    private String seqb_type;

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

    public static UserRoleLink create(User user, Role role) {
        UserRoleLink.UserRoleLinkBuilder builder = UserRoleLink.builder();

        builder.user(user);
        builder.seqa_type("users");

        builder.role(role);
        builder.seqb_type("roles");

        return builder.build();
    }
}