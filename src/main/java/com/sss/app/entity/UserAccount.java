package com.sss.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "credentials")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seqp")
    private Long userId;

    @Column(name = "uid")
    private UUID uuid;

    @Column(name = "name", nullable = false, unique = true, length = 150)
    private String userName;

    @Column(name = "password", nullable = true, length = 255)
    private String passwordHash;

    @Column(name = "created_on")
    private OffsetDateTime createdAt;

    @Column(name = "modified_on")
    private OffsetDateTime updatedAt;

}
