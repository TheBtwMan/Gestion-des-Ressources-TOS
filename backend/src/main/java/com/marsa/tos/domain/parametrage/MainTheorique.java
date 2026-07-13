package com.marsa.tos.domain.parametrage;

import com.marsa.tos.domain.referentiel.Terminal;
import com.marsa.tos.domain.referentiel.Trafic;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;
import lombok.*;

/**
 * Ecran "Main théorique" (opération Manutention) : ressources humaines et matérielles
 * théoriques associées à un trafic, pour un terminal donné.
 */
@Entity
@Table(name = "param_main_theorique")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MainTheorique {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trafic_id", nullable = false)
    private Trafic trafic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terminal_id", nullable = false)
    private Terminal terminal;

    @Builder.Default
    @OneToMany(mappedBy = "mainTheorique", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MainRessourceHumaine> ressourcesHumaines = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "mainTheorique", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MainRessourceMaterielle> ressourcesMaterielles = new ArrayList<>();

    @Builder.Default
    @ManyToMany
    @JoinTable(name = "param_main_accessoire",
            joinColumns = @JoinColumn(name = "main_theorique_id"),
            inverseJoinColumns = @JoinColumn(name = "accessoire_id"))
    private List<com.marsa.tos.domain.referentiel.Accessoire> accessoires = new ArrayList<>();
}
