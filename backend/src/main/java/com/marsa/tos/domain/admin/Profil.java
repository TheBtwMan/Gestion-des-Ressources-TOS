package com.marsa.tos.domain.admin;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;
import lombok.*;

/** Ecran "Changement des profils par droits" : un profil = un ensemble de droits. */
@Entity
@Table(name = "admin_profil")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Profil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nom;

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "admin_profil_droit",
            joinColumns = @JoinColumn(name = "profil_id"),
            inverseJoinColumns = @JoinColumn(name = "droit_code"))
    private List<Droit> droits = new ArrayList<>();
}
