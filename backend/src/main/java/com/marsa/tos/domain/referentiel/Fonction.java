package com.marsa.tos.domain.referentiel;

import javax.persistence.*;
import lombok.*;

@Entity
@Table(name = "ref_fonction")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Fonction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String libelle;

    /** Fonction dont l'affectation autorise le choix d'une vacation (demi-shift) : ex. Grutier. */
    private boolean vacationAutorisee;
}
