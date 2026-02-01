package com.sss.app.entity.traveller;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

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

    private String type;

    private String salutation;

   @Column(name = "first_name", nullable = false)
    private String firstName;

    //@Column(name = "last_name")
    private String lastName;

    //@Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    private String age;
    private String nationality;

    @Column(unique = true)
    private String email;

    private String phone;

    //@Column(name = "is_deleted")
    //private Boolean isDeleted = false;


}
