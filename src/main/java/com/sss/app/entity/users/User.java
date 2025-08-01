package com.sss.app.entity.users;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.sss.app.entity.userrolelinks.UserRoleLink;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@DynamicInsert
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long seqp;

    @Column(insertable = false, updatable = false)
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

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<UserRoleLink> roles = new ArrayList<>();
}