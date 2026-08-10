package com.sss.app.entity.roles;

import com.sss.app.util.IdGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "roles")
@Data
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long seqp;

    @Column
    private String uid;

    // Null = platform-level role (visible/assignable across all organizations).
    @Column
    private Long orgId;

    @Column
    private String name;

    @Column
    private String label;

    @Column
    private Boolean is_active;

    @Column
    private Boolean is_archived;

    @PrePersist
    protected void onCreate() {
        if (this.uid == null) {
            this.uid = IdGenerator.newUid().toString();
        }
    }
}