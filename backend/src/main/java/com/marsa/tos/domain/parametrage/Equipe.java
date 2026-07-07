package com.marsa.tos.domain.parametrage;

import com.marsa.tos.domain.referentiel.Terminal;
import javax.persistence.*;
import lombok.*;

/** Ecran "Equipe" : équipes travaillant sur un terminal, opération Manutention. */
@Entity
@Table(name = "param_equipe")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Equipe {

    @Id
    private String id;

    @Column(nullable = false)
    private String nom;

    @Column(name = "responsable_matricule")
    private String responsableMatricule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terminal_id", nullable = false)
    private Terminal terminal;
}
