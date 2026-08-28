package com.sss.app.entity.traveller;

import com.sss.app.util.IdGenerator;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "traveller")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Traveller {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seqp;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID uid;

    private Long orgId;

    // ADULT / CHILD / INFANT (see TravellerType) — drives per-traveller
    // quote pricing once that's wired up; optional today.
    private String type;

    private String salutation;

   @Column(name = "first_name", nullable = false)
    private String firstName;

    //@Column(name = "last_name")
    private String lastName;

    //@Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    private Integer age;
    private String nationality;

    @Column
    private String email;

    private String phone;

    @Column(name = "passport_number")
    private String passportNumber;

    @Column(name = "passport_expiry")
    private LocalDate passportExpiry;

    // Country code (matches the app's other country-code fields), not a full name.
    @Column(name = "passport_issuing_country")
    private String passportIssuingCountry;

    //@Column(name = "is_deleted")
    //private Boolean isDeleted = false;

    @PrePersist
    protected void onCreate() {
        if (this.uid == null) {
            this.uid = IdGenerator.newUid();
        }
    }
}
