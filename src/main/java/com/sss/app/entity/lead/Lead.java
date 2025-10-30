package com.sss.app.entity.lead;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "leads")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seqp;
    private String name;
    private String email;
    private String phone;
    private String destination;     // e.g. "Bali"
    private Integer numberOfPeople; // e.g. 4
    private LocalDate travelDate;   // e.g. 2025-12-15
    private Integer durationDays;   // e.g. 7 days
    private Double budget;          // e.g. 150000.00
    private String status;          // e.g. "New", "Quoted", "Booked"

   // @Enumerated(EnumType.STRING)
    //private LeadSource source;      // e.g. INSTAGRAM, WEBSITE

    @Column(length = 500)
    private String notes;
}
