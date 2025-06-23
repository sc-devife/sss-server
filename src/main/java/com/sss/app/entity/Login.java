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
@Table(name = "login1")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Login {
    @Id
    @Column
    private String username;

    @Column
    private String password;

    @Column
    private String email;

    @Column(name = "reset_token")
    private String resetToken;
    private LocalDateTime tokenExpiry;
}
