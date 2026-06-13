package com.sss.app.entity.library.escapesource;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "trip_source_direct_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EscapeSourceDirectDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seqp;

    @OneToOne
    @JoinColumn(name = "seqa")
    private EscapeSource escapeSource;
}
