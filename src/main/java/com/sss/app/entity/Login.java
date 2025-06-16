package com.sss.app.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "login1")
@Data
public class Login {
    @Id
    @Column
    private String username;

    @Column
    private String password;
}
