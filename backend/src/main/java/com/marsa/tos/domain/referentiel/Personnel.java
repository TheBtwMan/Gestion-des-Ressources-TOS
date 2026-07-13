package com.marsa.tos.domain.referentiel;

import jakarta.persistence.*;
import lombok.*;

/**
 * Personnel rapatrié depuis HR Access (SFD : "Les matricules du personnel sont rapatriés à partir HR Access").
 * Simulé ici via le mock TOS.
 */
@Entity
@Table(name = "ref_personnel")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Personnel {

    @Id
    private String matricule;

    @Column(nullable = false)
    private String nom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fonction_id", nullable = false)
    private Fonction fonction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipe_id")
    private com.marsa.tos.domain.parametrage.Equipe equipe;

    @Builder.Default
    private boolean actif = true;
}
