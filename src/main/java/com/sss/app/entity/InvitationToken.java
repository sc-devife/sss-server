package com.sss.app.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
@Entity
public class InvitationToken {
    @jakarta.persistence.Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Setter
    private String email;
    @Setter
    @Getter
    private String token;
    //private LocalDateTime expiryDate;
    @Setter
    @Getter
    private boolean used;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    //@jakarta.persistence.Id
    //@GeneratedValue(strategy = GenerationType.IDENTITY)
    //private Long id;
    //private String token;
    //private String email;

    public InvitationToken()
    {
        this.createdAt = LocalDateTime.now();
        this.expiresAt = createdAt.plusDays(7); // Default expiration time of 7 days
    }

    // Getters and Setters
   // public Long getId() {
    //    return id;
   // }

   // public void setused(Long id) {
   //     this.id = id;
   // }

    // public LocalDateTime getCreatedAt() {
    //    return createdAt;
   // }

   // public void setCreatedAt(LocalDateTime createdAt) {
     //   this.createdAt = createdAt;
   // }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiryDate(LocalDateTime expiresAt) {
       this.expiresAt = expiresAt;
    }

   // public String getEmail() {
     //   return email;
    //}

}
