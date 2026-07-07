package com.marsa.tos.domain.parametrage;

import com.marsa.tos.common.Enums.JourType;
import com.marsa.tos.common.Enums.SemaineType;
import com.marsa.tos.domain.referentiel.Terminal;
import javax.persistence.*;
import lombok.*;

/** Ecran "Mode de travail" - un par terminal, opération Manutention. */
@Entity
@Table(name = "param_mode_travail")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModeTravail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terminal_id", nullable = false, unique = true)
    private Terminal terminal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SemaineType semaine;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JourType jour;
}
