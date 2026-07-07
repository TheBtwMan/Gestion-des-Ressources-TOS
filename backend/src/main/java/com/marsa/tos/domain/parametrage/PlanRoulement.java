package com.marsa.tos.domain.parametrage;

import com.marsa.tos.common.Enums.Shift;
import com.marsa.tos.common.Enums.TypeRoulement;
import java.time.LocalDate;
import javax.persistence.*;
import lombok.*;

/** Ecran "Plan de roulement" : association équipe <-> shift sur une période (mois ou semaine). */
@Entity
@Table(name = "param_plan_roulement")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanRoulement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeRoulement typeRoulement;

    @Column(nullable = false)
    private LocalDate dateDebut;

    @Column(nullable = false)
    private LocalDate dateFin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipe_id", nullable = false)
    private Equipe equipe;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Shift shift;
}
