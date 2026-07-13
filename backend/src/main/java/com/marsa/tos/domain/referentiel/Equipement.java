package com.marsa.tos.domain.referentiel;

import jakarta.persistence.*;
import lombok.*;

/** Equipement rapatrié d'APIPRO selon disponibilité (SFD). */
@Entity
@Table(name = "ref_equipement")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Equipement {

    @Id
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "famille_id", nullable = false)
    private EquipementFamille famille;

    @Builder.Default
    private boolean disponible = true;
}
