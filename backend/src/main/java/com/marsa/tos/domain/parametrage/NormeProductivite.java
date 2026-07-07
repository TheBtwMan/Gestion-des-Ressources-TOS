package com.marsa.tos.domain.parametrage;

import com.marsa.tos.common.Enums.NatureSuivi;
import com.marsa.tos.common.Enums.Sens;
import com.marsa.tos.domain.referentiel.Trafic;
import javax.persistence.*;
import lombok.*;

/** Ecran "Norme de productivité" : dépend du trafic, de la main théorique et du sens. */
@Entity
@Table(name = "param_norme_productivite")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NormeProductivite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trafic_id", nullable = false)
    private Trafic trafic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "main_theorique_id", nullable = false)
    private MainTheorique mainTheorique;

    @Column(nullable = false)
    private String mode; // ex. T/M, T/M/Shift

    @Column(nullable = false)
    private Integer norme;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Sens sens;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NatureSuivi natureSuivi;
}
