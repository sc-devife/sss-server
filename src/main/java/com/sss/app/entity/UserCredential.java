package com.sss.app.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.util.UUID;

@Entity
@Table(name = "user_credentials")
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
@Builder
public class UserCredential {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long seqp;

    @Column
    private Long seqa;

    @Column
    private String seqa_type;

    @Column
    private UUID uid;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String password_hash;

    public static UserCredential create(Long seqa, String password) {
        UserCredential.UserCredentialBuilder builder = UserCredential.builder();
        builder.seqa(seqa);
        builder.seqa_type("users");
        builder.password_hash(password);

        return builder.build();
    }

    public void update(String password) {
        this.setPassword_hash(password);
    }
}
