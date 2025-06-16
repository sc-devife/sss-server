package com.sss.app.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_sessions")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSession {
    @Id
    @Column(name = "username")
    private String username;
    @Column(name = "jwt_token", length = 2048)
    private String jwtToken;
    @Column(name = "last_accessed")
    private LocalDateTime lastAccessed;
}
