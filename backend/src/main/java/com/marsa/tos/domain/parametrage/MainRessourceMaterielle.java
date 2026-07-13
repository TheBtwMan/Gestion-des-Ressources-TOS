package com.marsa.tos.domain.parametrage;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.marsa.tos.domain.referentiel.EquipementFamille;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "param_main_ressource_materielle")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MainRessourceMaterielle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "main_theorique_id", nullable = false)
    @JsonIgnore
    private MainTheorique mainTheorique;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "famille_id", nullable = false)
    private EquipementFamille famille;
}
