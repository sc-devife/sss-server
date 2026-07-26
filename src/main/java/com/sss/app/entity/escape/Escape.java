package com.sss.app.entity.escape;

import com.sss.app.entity.lead.Lead;
import com.sss.app.entity.library.escapepoint.EscapePoint;
import com.sss.app.entity.library.escapesource.EscapeSource;
import com.sss.app.entity.traveller.Traveller;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "escapes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Escape {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seqp;

    private Long orgId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lead_id", nullable = false) //rename
    private Lead lead;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_id")
    private EscapeSource source;

    @ManyToMany
    @JoinTable(
            name = "escape_traveller",
            joinColumns = @JoinColumn(name = "escape_id"),
            inverseJoinColumns = @JoinColumn(name = "traveller_id")
    )
   // private List<Traveller> travellers;
    private Set<Traveller> travellers = new HashSet<>();;

    @ManyToMany
    @JoinTable(
            name = "escape_destination",
            joinColumns = @JoinColumn(name = "escape_id"),
            inverseJoinColumns = @JoinColumn(name = "escape_point_id")
    )
   // private List<EscapePoint> destinations;
    private Set<EscapePoint> destinations = new HashSet<>();

    private String status;

    private LocalDate startDate;
    private Integer numberOfDays;
    // ✅ AUTO CALCULATED
    private LocalDate endDate;

  /*  public void setTravellers(List<Escape> allById) {
    }*/
}
