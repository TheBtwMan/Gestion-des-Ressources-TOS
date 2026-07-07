package com.marsa.tos.domain.parametrage;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.marsa.tos.common.Enums.Emplacement;
import com.marsa.tos.domain.referentiel.Fonction;
import javax.persistence.*;
import lombok.*;

@Entity
@Table(name = "param_main_ressource_humaine")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MainRessourceHumaine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "main_theorique_id", nullable = false)
    @JsonIgnore
    private MainTheorique mainTheorique;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fonction_id", nullable = false)
    private Fonction fonction;

    @Column(nullable = false)
    private Integer nombreTotal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Emplacement emplacement;

    private Integer maxNombre;

    @Builder.Default
    private boolean maxObligatoire = false;
}
