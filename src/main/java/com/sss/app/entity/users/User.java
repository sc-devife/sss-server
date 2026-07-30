package com.sss.app.entity.users;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.sss.app.dto.users.UserCreateRequestDto;
import com.sss.app.dto.users.UserUpdateRequestDto;
import com.sss.app.entity.userrolelinks.UserRoleLink;
import com.sss.app.util.CompareUtil;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long seqp;

    @Column(insertable = false, updatable = false)
    private String uid;

    @Column(name = "user_id", unique = true)
    private String userId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column
    private Long orgId;

    @Column
    private Long invitedBy;

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

    // Section 15 / Lead Assignment Engine (Section 5) fields.
    @Column(name = "is_specialist")
    private Boolean isSpecialist;

    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.ARRAY)
    @Column(name = "specialist_destinations", columnDefinition = "bigint[]")
    private List<Long> specialistDestinations;

    @Column(name = "max_concurrent_assignments")
    private Integer maxConcurrentAssignments;

    @Column(name = "eligible_for_priority_leads")
    private Boolean eligibleForPriorityLeads;

    // Section 5 Excel writeup: "Enable/Disable receiving leads temporarily."
    @Builder.Default
    @Column(name = "accepting_leads")
    private Boolean acceptingLeads = true;

    // Org admins can block a user to immediately revoke login/API access
    // without deleting their account or role history.
    @Column(name = "blocked")
    private Boolean blocked;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<UserRoleLink> roles = new ArrayList<>();

    public static User create(UserCreateRequestDto dto) {
        UserBuilder builder = User.builder();

        //Keeping the username as email for now, can be changed later
        builder.name(dto.getEmail());
        builder.email(dto.getEmail());

        builder.first_name(dto.getFirst_name());
        builder.last_name(dto.getLast_name());
        builder.contact_number(dto.getContact_number());

        return builder.build();
    }

    public void update(UserUpdateRequestDto dto) {
        if (CompareUtil.hasChanged(dto.getFirst_name(), this.first_name)) {
            this.first_name = dto.getFirst_name();
        }

        if (CompareUtil.hasChanged(dto.getLast_name(), this.last_name)) {
            this.last_name = dto.getLast_name();
        }

        if (CompareUtil.hasChanged(dto.getContact_number(), this.contact_number)) {
            this.contact_number = dto.getContact_number();
        }
    }
}